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

class ApiActor extends Actor with ApiService{
  def actorRefFactory = context
  
  // Route requests to our HttpService
  def receive = runRoute(apiRoute)
}

trait ApiService extends HttpService {
  val reductoRouter = actorRefFactory.actorOf(Props[ReductoActor].withRouter(
   	ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
   	ClusterRouterSettings(
   	totalInstances = 100, maxInstancesPerNode = 1,
   	allowLocalRoutees = true, useRole = Some("nlp-frontend")))),
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
                          entity(as[HttpRequest]){
                            obj => complete{
                                    var response:Any = null
                              
                                    var  urlField = obj.uri.query.get("url");
                                    var  weightField = obj.uri.query.get("weight");
                                    var  metadataField = obj.uri.query.get("metadata");
                                    var  socialField = obj.uri.query.get("social");
                                    var  breakdownField = obj.uri.query.get("breakdown");
                                    var  sentencesField = obj.uri.query.get("sentences");
                                    
                                    var url:String = null
                                    var weight:Boolean = false
                                var metadata:Boolean = false
                                var social:Boolean = false
                                var breakdown:Boolean = false
                                var sentences:Int = 3

                                if(urlField != None){
                                        url = urlField.get
                                        response = obj.uri
                                }
                                else{
                                        response = "Error: No url"
                                }
                              
                                    if(weightField != None)
                                            weight = weightField.get.toBoolean
                                            println(weight)
                                    if(metadataField != None)
                                            metadata = metadataField.get.toBoolean
                                    if(socialField != None)
                                            social = socialField.get.toBoolean  
                                    if(breakdownField != None)
                                            breakdown = breakdownField.get.toBoolean
                                    if(sentencesField != None)
                                            sentences = sentencesField.get.toInt

                                response.toString
                            }
                          }
                  }
                }~
                post{
                  respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[String]){
                                  obj => complete{
                              
                                          val request = Json.parse(obj)
                                          var response:Any = null
                              
                                          val urlField = (request \ "url").asOpt[String]                              
                                          val weightField = (request \ "weight").asOpt[Boolean]
                                          val metadataField = (request \ "metadata").asOpt[Boolean]
                                          val socialField = (request \ "social").asOpt[Boolean]
                                  val breakdownField = (request \ "breakdown").asOpt[Boolean]                              
                                  val sentencesField = (request\ "sentences").asOpt[Int]

                                  var url:String = null
                                  var weight:Boolean = false
                                  var metadata:Boolean = false
                                  var social:Boolean = false
                                  var breakdown:Boolean = false
                                  var sentences:Int = 3

                                 if(urlField != None){
                                    url = urlField.get
                                    response = obj.asJson.prettyPrint
                                     }
                                 else{
                                    response = "Error: URL field missing"
                                 }
                              
                                 if(weightField != None)
                                        weight = weightField.get
                             if(metadataField != None)
                                        metadata = metadataField.get
                             if(socialField != None)
                                        social = socialField.get                              
                             if(breakdownField != None)
                                        breakdown = breakdownField.get
                                 if(sentencesField != None)
                                        sentences = sentencesField.get
                              
                                 response.toString
                                  }
                          }
                  }        
                }
        }~
        path("text"){
                get{
                          respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[HttpRequest]){
                            obj => complete{
                                    var response:Any = null
                              
                                    var  headlineField = obj.uri.query.get("headline");
                                    var  textField = obj.uri.query.get("text");
                                    var  weightField = obj.uri.query.get("weight");
                                    var  metadataField = obj.uri.query.get("metadata");
                                    var  socialField = obj.uri.query.get("social");
                                    var  breakdownField = obj.uri.query.get("breakdown");
                                    var  sentencesField = obj.uri.query.get("sentences");
                                    
                                    var headline:String = null
                                    var text:String = null
                                    var weight:Boolean = false
                                var metadata:Boolean = false
                                var social:Boolean = false
                                var breakdown:Boolean = false
                                var sentences:Int = 3

                                if(headlineField != None && textField != None){
                                        headline = headlineField.get
                                        text = textField.get
                                        response = obj.uri
                                }
                                else{
                                        
                                        response = "Error: No headline or text fields"
                                }
                              
                                    if(weightField != None)
                                            weight = weightField.get.toBoolean
                                            println(weight)
                                    if(metadataField != None)
                                            metadata = metadataField.get.toBoolean
                                    if(socialField != None)
                                            social = socialField.get.toBoolean  
                                    if(breakdownField != None)
                                            breakdown = breakdownField.get.toBoolean
                                    if(sentencesField != None)
                                            sentences = sentencesField.get.toInt

                                    response.toString
                            }
                          }
                  }
                }
                post{
                  respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[String]){
                                  obj => complete{
                              
                              val request = Json.parse(obj)
                              var response:Any = null
                              
                              val headlineField = (request \ "headline").asOpt[String]
                              val textField = (request \ "text").asOpt[String]                              
                              val weightField = (request \ "weight").asOpt[Boolean]
                              val metadataField = (request \ "metadata").asOpt[Boolean]
                              val socialField = (request \ "social").asOpt[Boolean]
                              val breakdownField = (request \ "breakdown").asOpt[Boolean]
                              val sentencesField = (request\ "sentences").asOpt[Int]

                              var headline:String = null
                              var text:String = null        
                              var weight:Boolean = false
                              var metadata:Boolean = false
                              var social:Boolean = false
                              var breakdown:Boolean = false
                              var sentences:Int = 3

                              
                              if(headlineField != None && textField != None){
                                headline = headlineField.get
                                text = textField.get
                                
                                response = doReducto(headline, text)
                              }
                              
                              else{
                                response = "Error: No headline or text fields"
                              }
                              
                              if(weightField != None)
                                      weight = weightField.get
                              if(metadataField != None)
                                      metadata = metadataField.get
                              if(socialField != None)
                                      social = socialField.get                              
                              if(breakdownField != None)
                                      breakdown = breakdownField.get                              
                              if(sentencesField != None)
                                      sentences = sentencesField.get                              
                                      
                              response.toString
                              
                                  }
                          }
                  }        
                }
        }~
        path("weight"){
                get{
                          respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[HttpRequest]){
                            obj => complete{
                                    var response:Any = null
                              
                                    var  headlineField = obj.uri.query.get("headline");
                                    var  textField = obj.uri.query.get("text");
                                    var  socialField = obj.uri.query.get("social");
                                    
                                    var headline:String = null
                                    var text:String = null
                                var social:Boolean = false

                                if(headlineField != None && textField != None){
                                        headline = headlineField.get
                                        text = textField.get
                                        response = obj.uri
                                }
                                else{
                                        response = "Error: No headline or text fields"
                                }
                             
                                    if(socialField != None)
                                            social = socialField.get.toBoolean  

                                    response.toString
                            }
                          }
                  }
                }
                post{
                  respondWithMediaType(MediaTypes.`application/json`){
                          entity(as[String]){
                                  obj => complete{
                              
                              val request = Json.parse(obj)
                              var response:Any = null
                              
                              val headlineField = (request \ "headline").asOpt[String]
                              val textField = (request \ "text").asOpt[String]
                              val socialField = (request \ "social").asOpt[Boolean]

                              var headline:String = null
                              var text:String = null        
                              var social:Boolean = false
                              
                              if(headlineField != None && textField != None){
                                headline = headlineField.get
                                text = textField.get
                                response = obj.asJson.prettyPrint
                                  }
                              else{
                                response = "Error: No headline or text fields"
                              }

                          if(socialField != None)
                                      social = socialField.get                              

                              response.toString
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
        
	//================================================================================
	// Call Reducto
	//================================================================================
    def doReducto(headline:String, text:String) {
    	implicit val timeout = Timeout(500 seconds);
    	val response = (reductoRouter ? RawText(headline, text)).mapTo[SummaryResult]
    	complete(mapper.writeValueAsString(response))
    }
    
    
}


