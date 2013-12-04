package com.winston.api

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.winston.nlp.SummaryResult
import com.winston.nlp.worker.ReductoActor
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
import com.winston.nlp.transport.ReductoResponse
import com.winston.nlp.transport.messages._
import reflect.ClassTag
import akka.pattern.AskTimeoutException
import com.winston.nlp.scoring.ScoringActor
import com.winston.nlp.worker.ParseActor
import com.winston.nlp.worker.PackagingActor
import com.winston.nlp.worker.SplitActor
import com.winston.nlp.search.ElasticSearchActor
import com.winston.nlp.transport.ReductoResponse
import com.winston.nlp.worker.BatchParseActor

class ApiActor extends Actor with ApiService {
  def actorRefFactory = context
   	
implicit def ReductoExceptionHandler(implicit log: LoggingContext) =
  ExceptionHandler {
    case e: NoSuchElementException => ctx =>
      println("no element")
      val err = "\n--No Such Element Exception--"
      log.warning("{}\n encountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(BadRequest, "Ensure all required fields are present.")
    
    case e: JsonParseException => ctx =>
      println("json parse")
      val err = "\n--Exception parsing input--"
      log.warning("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Ensure all required fields are present with all Illegal characters properly escaped")
      
    case e: AskTimeoutException => ctx =>
      println("Ask Timeout")
      val err = "\n--Timeout Exception--"
      log.warning("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(RequestTimeout, "Server Timeout")
    
    case e: Exception => ctx => 
      println("Unknown")
      val err = "\n--Unknon Exception--"
      log.warning("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Internal Server Error")
  }
    
  // Route requests to our HttpService
  def receive = runRoute(apiRoute)
  
}

trait ApiService extends HttpService {
  
  // Easy role change for debugging
  val role = "reducto-frontend"
    
  // Splitting router
  val splitRouter = actorRefFactory.actorOf(Props[SplitActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 5,
	allowLocalRoutees = true, useRole = Some(role)))),
	name = "splitRouter")
	  
  // Parsing router
  val parseRouter = actorRefFactory.actorOf(Props[ParseActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.HeapMetricsSelector), 
	ClusterRouterSettings(
<<<<<<< HEAD
	totalInstances = 100, maxInstancesPerNode = 3,
=======
	totalInstances = 100, maxInstancesPerNode = 10,
>>>>>>> parent of cf51f57... 5 parsers
	allowLocalRoutees = true, useRole = Some(role)))),
	name = "parseRouter")

  // Scoring router
  val batchParseRouter = actorRefFactory.actorOf(Props(classOf[BatchParseActor], parseRouter).withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 5,
	allowLocalRoutees = true, useRole = Some(role)))),
	name = "batchParseRouter")
	
  // Search router
  val elasticSearchRouter = actorRefFactory.actorOf(Props[ElasticSearchActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 5,
	allowLocalRoutees = true, useRole = Some(role)))),
	name = "elasticSearchRouter")
	  
  // Scoring router
  val scoringRouter = actorRefFactory.actorOf(Props(classOf[ScoringActor], elasticSearchRouter).withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 5,
	allowLocalRoutees = true, useRole = Some(role)))),
	name = "scoringRouter")

  // Package router
  val packageRouter = actorRefFactory.actorOf(Props[PackagingActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 5,
	allowLocalRoutees = true, useRole = Some(role)))),
	name = "packageRouter")
  
  // Reducto Router
  val reductoRouter = actorRefFactory.actorOf(Props(classOf[ReductoActor],splitRouter, parseRouter, scoringRouter, packageRouter, batchParseRouter).withRouter(
   	ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
   	ClusterRouterSettings(
   	totalInstances = 100, maxInstancesPerNode = 5,
   	allowLocalRoutees = true, useRole = Some(role)))),
   	name = "reductoActors")
  
  // Mapper	
  val mapper = new ObjectMapper() with ScalaObjectMapper
      mapper.registerModule(DefaultScalaModule)
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
       
  implicit val timeout = Timeout(5 seconds);
  
  val apiRoute =
        path(""){
          complete("Reducto API")
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
                }
                          
                post{
                  respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[String]){ obj => 
                            val start = Platform.currentTime
                          	val request = new ReductoRequest(obj, "URL")
                            complete {
                              reductoRouter.ask(RequestContainer(request))(100.seconds).mapTo[ResponseContainer] map { container => 
                                container.resp.finishResponse(start, mapper) 
                              }
                            }
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
                }
                          
                post{
                  respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[String]){ obj => 
                            val start = Platform.currentTime
                          	val request = new ReductoRequest(obj, "TEXT")
                            complete {
                              reductoRouter.ask(RequestContainer(request))(100.seconds).mapTo[ResponseContainer] map { container => 
                                container.resp.finishResponse(start, mapper) 
                              }
                            }
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
                    	    container.fake(start, res.set, mapper)
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
        path(RestPath) { path =>
          getFromFile("/var/www/akka-public/" + path)
        }
}

