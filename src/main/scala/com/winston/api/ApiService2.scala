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

class ApiActor2 extends Actor with ApiService2{
  def actorRefFactory = context
  
implicit def ReductoExceptionHandler(implicit log: LoggingContext) =
  ExceptionHandler {
    case e: NoSuchElementException => ctx =>
      val err = "--No Such Element Exception--"
      log.warning("{}\n encountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(BadRequest, "Ensure all required fields are present.")
    
    case e: JsonParseException => ctx =>
      val err = "--Exception parsing input--"
      log.warning("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Ensure all required fields are present with all Illegal characters properly escaped")
      
    case e: Exception => ctx => 
      val err = "--Unknon Exception--"
      log.warning("{}\nencountered while handling request:\n {}\n\n{}", err, ctx.request,e)
      ctx.complete(InternalServerError, "Internal Server Error")
  }
  
  // Route requests to our HttpService
  def receive = runRoute(apiRoute)
  
}

trait ApiService2 extends HttpService {
  val reductoRouter = actorRefFactory.actorOf(Props[ReductoActor].withRouter(
   	ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
   	ClusterRouterSettings(
   	totalInstances = 100, maxInstancesPerNode = 1,
   	allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
   	name = "ReductoActors")
  
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
                                container.resp.finishResponse(start);
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
                                container.resp.finishResponse(start) 
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
                                container.resp.finishResponse(start) 
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
                                container.resp.finishResponse(start) 
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
        }
}

