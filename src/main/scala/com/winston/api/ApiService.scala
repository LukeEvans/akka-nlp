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

class ApiActor extends Actor with ApiService {
  def actorRefFactory = context
   	
implicit def ReductoExceptionHandler(implicit log: LoggingContext) =
  ExceptionHandler {
    case e: NoSuchElementException => ctx =>
      val err = "\n--No Such Element Exception--"
      log.warning("{}\n encountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(BadRequest, "Ensure all required fields are present.")
    
    case e: JsonParseException => ctx =>
      val err = "\n--Exception parsing input--"
      log.warning("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Ensure all required fields are present with all Illegal characters properly escaped")
      
    case e: AskTimeoutException => ctx => 
      val err = "\n--Timeout Exception--"
      log.warning("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(RequestTimeout, "Server Timeout")
    
    case e: Exception => ctx => 
      val err = "\n--Unknon Exception--"
      log.warning("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Internal Server Error")
  }
    
  // Route requests to our HttpService
  def receive = runRoute(apiRoute)
  
}

trait ApiService extends HttpService {
  
  // Splitting router
  val splitRouter = actorRefFactory.actorOf(Props[SplitActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 1,
	allowLocalRoutees = true, useRole = Some("reducto-backend")))),
	name = "splitRouter")
	  
  // Parsing router
  val parseRouter = actorRefFactory.actorOf(Props[ParseActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.HeapMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 4,
	allowLocalRoutees = true, useRole = Some("reducto-backend")))),
	name = "parseRouter")

  // Search router
  val elasticSearchRouter = actorRefFactory.actorOf(Props[ElasticSearchActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 1,
	allowLocalRoutees = true, useRole = Some("reducto-backend")))),
	name = "elasticSearchRouter")
	  
  // Scoring router
  val scoringRouter = actorRefFactory.actorOf(Props(classOf[ScoringActor], elasticSearchRouter).withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 1,
	allowLocalRoutees = true, useRole = Some("reducto-backend")))),
	name = "scoringRouter")

  // Package router
  val packageRouter = actorRefFactory.actorOf(Props[PackagingActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	ClusterRouterSettings(
	totalInstances = 100, maxInstancesPerNode = 1,
	allowLocalRoutees = true, useRole = Some("reducto-backend")))),
	name = "packageRouter")
  
  // Reducto Router
  val reductoRouter = actorRefFactory.actorOf(Props(classOf[ReductoActor],splitRouter, parseRouter, scoringRouter, packageRouter).withRouter(
   	ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
   	ClusterRouterSettings(
   	totalInstances = 100, maxInstancesPerNode = 1,
   	allowLocalRoutees = true, useRole = Some("reducto-backend")))),
   	name = "reductoActors")
  
  // Mapper	
  val mapper = new ObjectMapper() with ScalaObjectMapper
      mapper.registerModule(DefaultScalaModule)
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
       
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

