package com.winston.nlp.messages

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import java.util.ArrayList
import com.winston.nlp.SentenceSet
import com.winston.nlp.NLPSentence
import com.fasterxml.jackson.databind.JsonNode

trait request;
trait response;

// Initial raw Text
case class RawText(query:String, text:String) extends request

// Sentence set messages
case class SetContainer(set:SentenceSet) extends request

// Sentence 
case class SentenceContainer(sentence:NLPSentence) extends request

// Term frequency response
case class SingleTermFrequency(word:String, count:Long) extends request; 
case class TermFrequencyResponse(map:Map[String, Long]) extends response;

// Request actors to initilize any dangerous code they may have to start
case class InitRequest() extends request;

// HTTP Request
case class HttpObject(uri: String, obj: JsonNode = null, response: JsonNode = null, method: String = "GET") extends request;
case class JsonResponse(node: JsonNode) extends response;

// Stop words
case class StopPhrasesObject(phrases:ArrayList[String] = new ArrayList[String]) extends request;

// Long Container
case class LongContainer(long:Long) extends request;