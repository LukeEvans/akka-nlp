package com.winston.api

import akka.actor._
import akka.pattern.ask
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.unmarshalling._
import spray.json._
import spray.json.DefaultJsonProtocol._
import scala.concurrent.duration._
import spray.routing._
import play.api.libs.json._
import akka.actor.ActorRef
import akka.actor.Props
import com.winston.nlp.worker.ReductoActor
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterSettings
import com.winston.nlp.messages.RawText
import com.winston.nlp.SummaryResult
import akka.util.Timeout
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.winston.nlp.messages.SummaryResultContainer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import spray.util.LoggingContext
import spray.http.StatusCodes._


class ApiActor2 extends Actor with ApiService2{
  def actorRefFactory = context
  
implicit def myExceptionHandler(implicit log: LoggingContext) =
  ExceptionHandler {
    case e: NoSuchElementException => ctx =>
      log.warning("Request could not be handled normally: {} ", ctx.request)
      ctx.complete(BadRequest, "Ensure all required fields are present.")
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
        path("text"){
                post{
                  respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[String]){
                                  obj => 
                              
                              val request = Json.parse(obj)
                              var response:Any = null
                              
                              val headlineField = (request \ "headline").asOpt[String].get
                              val textField = (request \ "text").asOpt[String].get                              

                              complete(doReducto(headlineField, textField))
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
        
	//================================================================================
	// Call Reducto
	//================================================================================
    def doReducto(headline:String, text:String): Future[String] = {
    	implicit val timeout = Timeout(500 seconds);
    	
    	(reductoRouter ? RawText(headline, text)).mapTo[SummaryResultContainer] map { result =>
    	  mapper.writeValueAsString(result.summary)
    	}
    	
    }
    
}


