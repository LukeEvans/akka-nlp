package com.winston.nlp.transport

import com.winston.nlp.transport.messages.TransportMessage
import spray.http.HttpRequest
import com.fasterxml.jackson.databind.ObjectMapper
import spray.httpx.unmarshalling._
import spray.http._

class ReductoRequest extends TransportMessage {

	var request_type = "Undefined"
	var url:String = null
	var headline:String = null
	var text:String = null        
	var sentences:Int = 3
	var decay:Boolean = true
	var separationRules = true
	var ratio:Double = 0.0
	
	@transient
	val mapper = new ObjectMapper()
	
	//================================================================================
	// Constructors
	//================================================================================
	def this(request:String, rt:String) {
	  this()
	  
	  var cleanRequest = request.replaceAll("\\r", " ").replaceAll("\\n", " ").trim();
	  val reqJson = mapper.readTree(cleanRequest);
	  
	  url = if (!reqJson.path("url").isMissingNode()) reqJson.path("url").asText() else null
	  headline = if (!reqJson.path("headline").isMissingNode()) reqJson.path("headline").asText() else null
	  text = if (!reqJson.path("text").isMissingNode()) reqJson.path("text").asText() else null
	  sentences = if (!reqJson.path("sentences").isMissingNode()) reqJson.path("sentences").asInt() else 3
	  decay = if (!reqJson.path("decay").isMissingNode()) reqJson.path("decay").asBoolean() else true
	  separationRules = if (!reqJson.path("separationRules").isMissingNode()) reqJson.path("separationRules").asBoolean() else true
	  ratio = if(!reqJson.path("ratio").isMissingNode()) reqJson.path("ratio").asDouble() else 0.0
	  
  	  request_type = rt;
	}
	
	def this(request:HttpRequest, rt:String) {
	  this()
	  
	  url = if (request.uri.query.get("url") != None) request.uri.query.get("url").get else null
	  headline = if (request.uri.query.get("headline") != None) request.uri.query.get("headline").get else null
	  text = if (request.uri.query.get("text") != None) request.uri.query.get("text").get else null
	  sentences = if (request.uri.query.get("sentences") != None) request.uri.query.get("sentences").get.toInt else 3
	  decay = if (request.uri.query.get("decay") != None) request.uri.query.get("decay").get.toBoolean else true
	  separationRules = if (request.uri.query.get("separationRules") != None) request.uri.query.get("separationRules").get.toBoolean else true
	  ratio = if (request.uri.query.get("ratio") != None) request.uri.query.get("ratio").get.toDouble else 0.0
	  
	  request_type = rt;
	}
	
	def this(urlString:String, rt:String, idk:Boolean){
	  this()
	  url = urlString
	  request_type = rt
	}
	
	def this(headlineString:String, textString:String, rt:String){
	  this()
	  headline = headlineString
	  text = textString
	  request_type = rt
	}
	
	def setSent(sentences:Int):ReductoRequest ={
	  this.sentences = sentences
	  return this
	}
	
	def setDecay(decay:Boolean):ReductoRequest ={
	  this.decay = decay
	  return this
	}
	
	def setSeparation(separation:Boolean):ReductoRequest ={
	  this.separationRules = separation
	  return this
	}
}