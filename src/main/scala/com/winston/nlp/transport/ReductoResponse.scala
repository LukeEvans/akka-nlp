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

class ReductoResponse extends TransportMessage {
	var status = "OK"
	var time:String = null
	var article_headline:String = null;
	var summary:String = null
	var sentence_indices:ArrayList[Int] = _;
	
	@JsonIgnore
	var saliency_score:Float = 0;
	
	@JsonIgnore
	var relevance_score:Float = 0;
	
	@JsonIgnore
	var novelty_score:Float = 0;
	
	@JsonIgnore
	var social_salience:Float = 0;
	
	//================================================================================
	// Constructors
	//================================================================================
	def this(summaryResult:SummaryResult) {
	  this()
	  article_headline = summaryResult.article_headline
	  summary = summaryResult.summary
	  sentence_indices = summaryResult.sentence_indices
	  saliency_score = summaryResult.saliency_score
	  relevance_score = summaryResult.relevance_score
	  novelty_score = summaryResult.novelty_score
	  social_salience = summaryResult.social_salience
	}
	
	//================================================================================
	// Finish 
	//================================================================================
	def finishResponse(start:Long, mapper:ObjectMapper): String = {
	  val stop = Platform.currentTime
	  val duration = stop - start
	  time = duration + " ms"
	  val jsonString = mapper.writeValueAsString(this)
	  jsonString;
	}
	
	def markCompleteTime(start:Long) {
		val stop = Platform.currentTime
		val duration = stop - start
	    time = duration + " ms"
	}
	
	def finishSetResponse(start:Long, res:SentenceSet, mapper:ObjectMapper): String = {
	  val stop = Platform.currentTime
	  val duration = stop - start
	  val fakeTime = duration + " ms"
	  
	  val m:LinkedHashMap[String, String] = new LinkedHashMap[String, String]()
	  val jsonString = mapper.writeValueAsString(res)
	  jsonString;
	}
	
	
	def setStatus(status:String):ReductoResponse = {
	  this.status = status
      this
	}
}