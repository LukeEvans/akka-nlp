package com.winston.nlp.transport.messages

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import java.util.ArrayList
import com.winston.nlp.SentenceSet
import com.winston.nlp.NLPSentence
import com.fasterxml.jackson.databind.JsonNode
import com.winston.nlp.SummaryResult
import com.winston.nlp.transport.ReductoRequest
import com.winston.nlp.transport.ReductoResponse
import reflect.ClassTag
import com.winston.nlp.transport.ErrorResponse

trait request;
trait response;

// Reducto Request and Response
case class RequestContainer(req:ReductoRequest) extends request
case class ResponseContainer(resp:ReductoResponse) extends response
case class HammerRequestContainer(req:ReductoRequest) extends request

// Sentence set messages
case class SetContainer(set:SentenceSet, number:Int) extends request

// Sentence 
case class SentenceContainer(val sentence:NLPSentence) extends request

// Term frequency response
case class SingleTermFrequency(word:String, count:Long) extends request
case class TermFrequencyResponse(mapObject:Map[String, Long]) extends response
case class TermFrequencyBulkReq(wordList:List[String]) extends request

// Request actors to initilize any dangerous code they may have to start
case class InitRequest() extends request

// HTTP Request
case class HttpObject(uri: String, obj: JsonNode = null, response: JsonNode = null, method: String = "GET") extends request
case class JsonResponse(node: JsonNode) extends response;

// Stop words
case class StopPhrasesObject(phrases:ArrayList[String] = new ArrayList[String]) extends request

// Long Container
case class LongContainer(long:Long) extends request

// Url String Container
case class URLContainer(url:String) extends request
case class URLTextResponse(extractionTuple:(String, String)) extends request

// Circuit Break Exception
case class CircuitBreakException(message:ErrorResponse) extends response
