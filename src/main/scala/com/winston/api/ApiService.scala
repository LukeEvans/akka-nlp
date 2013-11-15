package com.winston.api

import org.json4s.DefaultFormats
import org.json4s.Formats
import org.json4s.JsonAST.JObject

import akka.actor.Actor
import spray.http._
import spray.http.MediaTypes._
import spray.httpx.Json4sSupport
import spray.httpx.unmarshalling._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.routing._
import play.api.libs.json._

class ApiActor extends Actor with getService{
  
  def actorRefFactory = context
  
  def receive = runRoute(getRoute)

}

object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}

trait getService extends HttpService{
  val getRoute =
	path(""){
		get {
			complete("Reducto API")
		} ~
		post{
			complete("Reducto API")
		}
	}~
	path("url"){
		get{
		  respondWithMediaType(MediaTypes.`application/json`){
			  entity(as[HttpRequest]){
			    obj => complete{
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
		    	    }
		    	    else{
		    	    	"Error: No url"
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

			    	"ok"
			    }
			  }
		  }
		}~
		post{
		  respondWithMediaType(MediaTypes.`application/json`){
			  entity(as[String]){
				  obj => complete{
		    	  
		    	  val request = Json.parse(obj)
		    	  
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
				  }
		    	  else{
		    	    "Error: No url"
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
		    	  
  		  
		  
		    	  val response = obj.asJson
		    	  val responseString =response.prettyPrint
		    	  responseString
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
		    	    }
		    	    else{
		    	    	"Error: No headline or text fields"
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

			    	"ok"
			    }
			  }
		  }
		}
		post{
		  respondWithMediaType(MediaTypes.`application/json`){
			  entity(as[String]){
				  obj => complete{
		    	  
		    	  val request = Json.parse(obj)
		    	  
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
				  }
		    	  else{
		    	    "Error: No headline or text fields"
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
		    		  
		    	  val response = obj.asJson
		    	  val responseString =response.prettyPrint
		    	  responseString
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
			    	var  headlineField = obj.uri.query.get("headline");
			    	var  textField = obj.uri.query.get("text");
			    	var  socialField = obj.uri.query.get("social");
			    	
			    	var headline:String = null
	    			var text:String = null
		    	    var social:Boolean = false

		    	    if(headlineField != None && textField != None){
		    	    	headline = headlineField.get
		    	    	text = textField.get
		    	    }
		    	    else{
		    	    	"Error: No headline or text fields"
		    	    }
		    	 
			    	if(socialField != None)
			    		social = socialField.get.toBoolean  

			    	"ok"
			    }
			  }
		  }
		}
		post{
		  respondWithMediaType(MediaTypes.`application/json`){
			  entity(as[String]){
				  obj => complete{
		    	  
		    	  val request = Json.parse(obj)
		    	  
		    	  val headlineField = (request \ "headline").asOpt[String]
    			  val textField = (request \ "text").asOpt[String]
		    	  val socialField = (request \ "social").asOpt[Boolean]

		    	  var headline:String = null
		    	  var text:String = null	
		    	  var social:Boolean = false
		    	  
		    	  if(headlineField != None && textField != None){
		    	    headline = headlineField.get
		    	    text = textField.get
				  }
		    	  else{
		    	    "Error: No headline or text fields"
		    	  }

		          if(socialField != None)
		    		  social = socialField.get		    	  

		    	  val response = obj.asJson
		    	  val responseString =response.prettyPrint
		    	  responseString
				  }
			  }
		  }	
		}
	}~
	path("short"){
		get{
		  	respondWithMediaType(MediaTypes.`application/json`){
			  entity(as[HttpRequest]){
				  obj =>{
				    complete{"OK."}
				  }
			  }
		  }
		}
		post{
		  respondWithMediaType(MediaTypes.`application/json`){
			  entity(as[String]){
				obj=>{
					complete{"OK."}
				}
			  }
		  }	
		}
	}
}