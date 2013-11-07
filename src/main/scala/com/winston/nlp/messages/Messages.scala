package com.winston.nlp.messages

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

import java.util.ArrayList

trait request;
trait response;

case class RawText(text: String) extends request
case class RawSentece(text: String) extends request

case class SplitSentences(sentences:List[String]) extends response
case class ParsedSentence(sentence:String, tree:String) extends response

case class NLPResponse(nlpSentences:List[ParsedSentence]) extends response;