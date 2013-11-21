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

class ReductoResponse extends TransportMessage {
	val status = "OK"
	var time:String = null
	var article_headline:String = null;
	var summary:String = null
	var sentence_indices:ArrayList[Int] = _;
	var saliency_score:Float = 0;
	var relevance_score:Float = 0;
	var novelty_score:Float = 0;
	var social_salience:Float = 0;
	
	@JsonIgnore
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
	
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
	def finishResponse(start:Long): String = {
	  val stop = Platform.currentTime
	  val duration = stop - start
	  time = duration + " ms"
	  val jsonString = mapper.writeValueAsString(this)
	  jsonString;
	}
}