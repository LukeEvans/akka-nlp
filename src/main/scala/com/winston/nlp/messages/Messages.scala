package com.winston.nlp.messages

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

import java.util.ArrayList

trait request;
trait response;

// Full text request and split response
case class RawText(text:String) extends request
case class SplitSentences(sentences:List[String]) extends response

// Split sentence request and parsed response
case class RawSentece(text:String) extends request
case class ParsedSentence(sentence:String, tree:String) extends response

// Scored sentence
case class ScoredSentence(text:String, tree:String, tfidf:Float, cosine:Float, predecayd_weight:Float, weight:Float);

// Score request and response
case class ScoreRequest(text:String, nlpSentences:List[ParsedSentence]) extends request;
case class ScoreRespones(scoredSentences:List[ScoredSentence]) extends response;

