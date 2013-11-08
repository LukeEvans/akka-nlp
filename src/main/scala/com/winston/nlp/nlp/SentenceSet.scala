package com.winston.nlp.nlp

import java.util.ArrayList
import scala.collection.JavaConversions._
import com.winston.nlp.annotate.NLPSplitter
import com.winston.nlp.messages.TransportMessage

class SentenceSet(query:String, full_text:String) extends TransportMessage {
	var sentences = new ArrayList[NLPSentence];
	var totalDocuments = 0;
	var totalNumberOfTerms = 0;
	
	def addSentence(sentence:NLPSentence) {
	  sentence.index = sentences.size();
	  sentences.add(sentence);
	}
	
	def replaceSentence(sentence:NLPSentence) {
	  if (sentence.index < sentences.size()) {
	    sentences.set(sentence.index, sentence);
	  }
	}
}