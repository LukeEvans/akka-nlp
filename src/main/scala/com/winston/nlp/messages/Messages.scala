package com.winston.nlp.messages

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import java.util.ArrayList
import com.winston.nlp.nlp.SentenceSet
import com.winston.nlp.nlp.SentenceSet
import com.winston.nlp.nlp.NLPSentence

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