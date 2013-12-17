package com.winston.nlp.transport

import com.winston.nlp.transport.messages.TransportMessage
import com.winston.nlp.SummaryResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.annotation.JsonIgnore
import scala.compat.Platform
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.DeserializationFeature
import java.util.ArrayList
import scala.collection.mutable.Map
import java.util.LinkedHashMap
import com.winston.nlp.SentenceSet

class ErrorResponse extends TransportMessage {
	val status = "OK"
	var time:String = null
	var error:String = null
	
	//================================================================================
	// Constructors
	//================================================================================
	def this(error:String) {
	  this()
	  this.error = error
	}
	
	//================================================================================
	// Finish 
	//================================================================================
	def finishResponse(start:Long, mapper:ObjectMapper): String = {
	  val stop = Platform.currentTime
	  val duration = stop - start
	  time = duration + " ms"
	  val jsonString = mapper.writeValueAsString(this)
	  println(jsonString)
	  jsonString;
	}
	
	def fake(start:Long, res:SentenceSet, mapper:ObjectMapper): String = {
	  val stop = Platform.currentTime
	  val duration = stop - start
	  val fakeTime = duration + " ms"
	  
	  val m:LinkedHashMap[String, String] = new LinkedHashMap[String, String]()
	  val jsonString = mapper.writeValueAsString(res)
	  jsonString;
	}
}