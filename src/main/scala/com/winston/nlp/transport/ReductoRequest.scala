package com.winston.nlp.transport

import com.winston.nlp.transport.messages.TransportMessage
import spray.http.HttpRequest
import com.fasterxml.jackson.databind.ObjectMapper
import spray.httpx.unmarshalling._
import spray.http._
import org.apache.http.client.utils.URLEncodedUtils
import com.fasterxml.jackson.databind.JsonNode
import java.nio.charset.Charset
import java.net.URI

class ReductoRequest extends TransportMessage {

	var request_type = "Undefined"
	var url:String = null
	var headline:String = null
	var text:String = null        
	var weight:Boolean = false
	var metadata:Boolean = false
	var social:Boolean = false
	var breakdown:Boolean = false
	var sentences:Int = 3
	var decay:Boolean = true
	var separationRules = true
	var ratio:Double = 0
	
	@transient
	val mapper = new ObjectMapper()
	
	//================================================================================
	// Constructors
	//================================================================================
	def this(request:String, rt:String) {
	  this()
	  var cleanRequest:String = null
	  var reqJson:JsonNode = null
	  var full = "http://local:8080/text?" + request
//	  try{
//		  cleanRequest = request.replaceAll("\\r", " ").replaceAll("\\n", " ").trim();
//		  reqJson = mapper.readTree(cleanRequest);
//	  } 
	  var list = URLEncodedUtils.parse(new URI(full), "UTF-8")
	  
	  
	  
	  url = if (!reqJson.path("url").isMissingNode()) reqJson.path("url").asText() else null
	  headline = if (!reqJson.path("headline").isMissingNode()) reqJson.path("headline").asText() else null
	  text = if (!reqJson.path("text").isMissingNode()) reqJson.path("text").asText() else null
	  weight = if (!reqJson.path("weight").isMissingNode()) reqJson.path("weight").asBoolean() else false
	  metadata = if (!reqJson.path("metadata").isMissingNode()) reqJson.path("metadata").asBoolean() else false
	  social = if (!reqJson.path("social").isMissingNode()) reqJson.path("social").asBoolean() else false
	  breakdown = if (!reqJson.path("breakdown").isMissingNode()) reqJson.path("breakdown").asBoolean() else false
	  sentences = if (!reqJson.path("sentences").isMissingNode()) reqJson.path("sentences").asInt() else 3
	  decay = if (!reqJson.path("decay").isMissingNode()) reqJson.path("decay").asBoolean() else true
	  separationRules = if (!reqJson.path("separationRules").isMissingNode()) reqJson.path("separationRules").asBoolean() else true
	  ratio = if(!reqJson.path("ratio").isMissingNode()) reqJson.path("ratio").asDouble() else 0
	  
  	  request_type = rt;
	}
	
	def this(request:HttpRequest, rt:String) {
	  this()
	  
	  url = if (request.uri.query.get("url") != null) request.uri.query.get("url").get else null
	  headline = if (request.uri.query.get("headline") != null) request.uri.query.get("headline").get else null
	  text = if (request.uri.query.get("text") != null) request.uri.query.get("text").get else null
	  weight = if (request.uri.query.get("weight") != null) request.uri.query.get("weight").get.toBoolean else false
	  metadata = if (request.uri.query.get("metadata") != null) request.uri.query.get("metadata").get.toBoolean else false
	  social = if (request.uri.query.get("social") != null) request.uri.query.get("social").get.toBoolean else false
	  breakdown = if (request.uri.query.get("breakdown") != null) request.uri.query.get("breakdown").get.toBoolean else false
	  sentences = if (request.uri.query.get("sentences") != null) request.uri.query.get("sentences").get.toInt else 3
	  decay = if (request.uri.query.get("decay") != null) request.uri.query.get("decay").get.toBoolean else true
	  separationRules = if (request.uri.query.get("separationRules") != null) request.uri.query.get("separationRules").get.toBoolean else true
	  ratio = if(request.uri.query.get("ratio")!= null) request.uri.query.get("ratio").get.toDouble else 0
	  
	  request_type = rt;
	}
}