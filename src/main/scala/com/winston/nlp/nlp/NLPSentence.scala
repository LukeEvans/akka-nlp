package com.winston.nlp.nlp

import java.util.ArrayList
import scala.collection.JavaConversions._
import com.winston.nlp.messages.TransportMessage

class NLPSentence(v:String) extends TransportMessage {
	val value = v;
	var index = -1;
	var words =  new ArrayList[NLPWord]
	var cosine_score = 0.0;
	var cummulative_tfidf = 0.0;
	var predecayd_weight = 0.0;
	var weight = 0.0;
	var tree = "";
	
	def addWord(word:String) {
	  words.add(new NLPWord(word));
	}
	
	def putTree(t:String) {
	  tree = t;
	}
}