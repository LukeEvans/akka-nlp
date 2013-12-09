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
	val status = "OK"
	var time:String = null
	var article_headline:String = null;
	var summary:String = null
	var sentence_indices:ArrayList[Int] = _;
	var saliency_score:Float = 0;
	var relevance_score:Float = 0;
	var novelty_score:Float = 0;
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
	
	def fake(start:Long, res:SentenceSet, mapper:ObjectMapper): String = {
	  val stop = Platform.currentTime
	  val duration = stop - start
	  val fakeTime = duration + " ms"
	  
	  val m:LinkedHashMap[String, String] = new LinkedHashMap[String, String]()
//	  m.put("took", fakeTime)
//	  m.put("data", res)
//	  m.put("fake","'Cyber Monday,' the term widely used by retailers and the press to describe the shopping spree that occurs the Monday after Thanksgiving weekend in the US, was first invoked in 2005 by Shop.org, a website run by a retail trade group. Its savvy marketing paid off: in the intervening eight years, Cyber Monday has quickly become one of the biggest shopping days of the year for Americans, with steep discounts from both online merchants and their brick and mortar counterparts (although, it's worth noting many of those discounts were built into the original prices of products). This year, Cyber Monday may even surpass Black Friday in terms of total sales. We've gathered the best deals leading up to and on Cyber Monday 2013, December 2nd, to help you plow through your gift checklist that much faster and get you on to those holiday celebrations.")
	  val jsonString = mapper.writeValueAsString(res)
	  jsonString;
	}
}