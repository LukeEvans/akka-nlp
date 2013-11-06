package com.winston.nlp.messages

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

import java.util.ArrayList

trait request

case class RawText(text: String) extends request

case class SplitSentences(sentences:ArrayList[String]) extends request
