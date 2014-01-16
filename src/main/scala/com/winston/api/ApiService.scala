package com.winston.api

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.winston.nlp.SummaryResult
import com.winston.nlp.pipeline.ReductoActor
import akka.actor._
import akka.actor.ActorRef
import akka.actor.Props
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.ClusterRouterSettings
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json._
import spray.http._
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.unmarshalling._
import spray.json.DefaultJsonProtocol._
import spray.routing._
import spray.util.LoggingContext
import com.winston.nlp.transport.ReductoRequest
import com.fasterxml.jackson.core.JsonParseException
import scala.compat.Platform
import com.winston.nlp.transport.messages._
import reflect.ClassTag
import akka.pattern.AskTimeoutException
import com.winston.nlp.scoring.ScoringActor
import com.winston.nlp.parse.ParseActor
import com.winston.nlp.packaging.PackagingActor
import com.winston.split.SplitActor
import com.winston.nlp.search.ElasticSearchActor
import com.winston.nlp.transport.ReductoResponse
import akka.routing.RoundRobinRouter
import com.winston.throttle.Dispatcher
import com.winston.throttle.TimerBasedThrottler
import com.winston.throttle.Throttler._

class ApiActor(reducto:ActorRef) extends Actor with ApiService {
  def actorRefFactory = context
   	
  println("Starting API Service actor...")
  val reductoRouter = reducto
  val dispatcher = actorRefFactory.actorOf(Props(classOf[Dispatcher], reductoRouter), "dispatcher")
  val throttler = actorRefFactory.actorOf(Props(new TimerBasedThrottler(new Rate(40, 1 seconds))))
  
  // Set the target
  throttler ! SetTarget(Some(dispatcher))
  
implicit def ReductoExceptionHandler(implicit log: LoggingContext) =
  ExceptionHandler {
    case e: NoSuchElementException => ctx =>
      println("no element")
      val err = "\n--No Such Element Exception--"
      log.error("{}\n encountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(BadRequest, "Ensure all required fields are present.")
    
    case e: JsonParseException => ctx =>
      println("json parse")
      val err = "\n--Exception parsing input--"
      log.error("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Ensure all required fields are present with all Illegal characters properly escaped")
      
    case e: AskTimeoutException => ctx =>
      println("Ask Timeout")
      val err = "\n--Timeout Exception--"
      log.error("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(RequestTimeout, "Server Timeout")
    
    case e: NullPointerException => ctx => 
      println("Null Pointer")
      val err = "\n--Exception parsing input--"
      log.error("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Ensure all required fields are present with all Illegal characters properly escaped")
    
    case e: Exception => ctx => 
      e.printStackTrace()
      println("Unknown")
      val err = "\n--Unknon Exception--"
      log.error("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Internal Server Error")
  }
    
  // Route requests to our HttpService
  def receive = runRoute(apiRoute)
  
}

trait ApiService extends HttpService {

  private implicit val timeout = Timeout(5 seconds);
  implicit val reductoRouter:ActorRef
  implicit val throttler:ActorRef

  
    // Mapper        
  val mapper = new ObjectMapper() with ScalaObjectMapper
      mapper.registerModule(DefaultScalaModule)
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      
  val apiRoute =
        path(""){
          get{
        	  getFromResource("web/index.html")
          }
        }~
        path("url"){
                get{
                  respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[HttpRequest]){ obj => 
                            val start = Platform.currentTime
                          	val request = new ReductoRequest(obj, "URL")
                            complete {
                              reductoRouter.ask(RequestContainer(request))(10.seconds).mapTo[ResponseContainer] map { container =>
                                container.resp.finishResponse(start, mapper);
                              }
                            }
                          }
                  }        
                }~                        
                post{
                  respondWithMediaType(MediaTypes.`application/json`){
                	  		formFields('url, 'sentences.?.as[Int], 'decay ? true, 'ratio ? 0.0, 'separationRules ? true){
                	  			(url, sentences, decay, ratio, separationRules) =>{
                	  				val start = Platform.currentTime
                	  				val request = new ReductoRequest(url, "URL", true).setDecay(decay).setSent(sentences).setSeparation(separationRules)
                	  				complete {
                	  					reductoRouter.ask(RequestContainer(request))(10.seconds).mapTo[ResponseContainer] map { container =>
                	  						container.resp.finishResponse(start, mapper);
                	  					}
                	  				}
                	  		  }
                	  		}~
                	  		entity(as[String]){ obj => ctx =>
                	  			val request = new ReductoRequest(obj, "TEXT")
                	  			println("Handling request")
                	  			initiateRequest(request, ctx)
                	  		} 
                  }        
                }
        }~        
        path("text"){
                get{
                  respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[HttpRequest]){ obj => 
                            val start = Platform.currentTime
                          	val request = new ReductoRequest(obj, "TEXT")
                            complete {
                              reductoRouter.ask(RequestContainer(request))(100.seconds).mapTo[ResponseContainer] map { container => 
                                container.resp.finishResponse(start, mapper) 
                              }
                            }
                          }
                  }        
                }~     
                post{              
                  respondWithMediaType(MediaTypes.`application/json`){
                         formFields('headline, 'text, 'sentences.?.as[Int], 'decay ? true, 'ratio ? 0.0, 'separationRules ? true){
                        	 (headline, text, sentences, decay, ratio, separationRules) =>{
                        		 val start = Platform.currentTime
                        		 val request = new ReductoRequest(headline, text, "URL").setDecay(decay).setSent(sentences).setSeparation(separationRules)
                        		 complete {
                        			 reductoRouter.ask(RequestContainer(request))(10.seconds).mapTo[ResponseContainer] map { container =>
                        			 container.resp.finishResponse(start, mapper);
                        			 }
                        		 }
                	  		  }
                	  	  }~
                          entity(as[String]){ obj => ctx =>
                          	val request = new ReductoRequest(obj, "TEXT")
                            initiateRequest(request, ctx)
                          }
                  }        
                }
        }~
        path("hammer") {
          post {
            respondWithMediaType(MediaTypes.`application/json`){
            	entity(as[String]){ obj =>
            	  	val start = Platform.currentTime
            		val request = new ReductoRequest(obj, "TEXT");
                    complete {
                    	reductoRouter.ask(HammerRequestContainer(request))(100.seconds).mapTo[SetContainer] map { res => 
                    	    val container = new ReductoResponse()
                    	    container.finishSetResponse(start, res.set, mapper)
                    	}
                     }            		
            	}
            }
          }
        }~
        path("health"){
                get{
                        complete{"OK."}
                }
                post{
                        complete{"OK."}
                }
        }~
        pathPrefix("css" / Segment) { file =>
          get {
            getFromResource("web/css/" + file)
          }
        }~
        path(RestPath) { path =>
          val resourcePath = "/usr/local/reducto-dist" + "/config/loader/" + path
          getFromFile(resourcePath)
        }
        
        def initiateRequest(request:ReductoRequest, ctx: RequestContext) {
            val dispatchReq = DispatchRequest(RequestContainer(request), ctx, mapper)
        	throttler.tell(Queue(dispatchReq), Actor.noSender)
        }
        
}

