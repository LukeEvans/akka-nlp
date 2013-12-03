package com.winston.nlp

import com.winston.nlp.transport.messages.TransportMessage
import java.util.ArrayList
import com.fasterxml.jackson.annotation.JsonIgnore
import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util.ArrayList

class SummaryResult extends TransportMessage {

	var article_headline:String = null;
	var summary:String = null;
	var sentence_indices:ArrayList[Int] = _;
	var saliency_score:Float = 0;
	var relevance_score:Float = 0;
	var novelty_score:Float = 0;
	var social_salience:Float = 0;
	
	@JsonIgnore
	var sentence_set:SentenceSet = _;
	
	@JsonIgnore
	var originalSentences:ArrayList[NLPSentence] = _;
	
	@JsonIgnore
	var processedSentences:ArrayList[NLPSentence] = _;
	
	def this(o:ArrayList[NLPSentence], p:ArrayList[NLPSentence], indices:ArrayList[Int]) {
	  this()
	  
	  sentence_indices = indices;
	  originalSentences = o;
	  processedSentences = p;
	  
	  // Remove unused sentences
	  removeUnusedSentences(sentence_indices)
	  
	  // Build Summary
	  buildSummary;
	}
	
	//================================================================================
	// Build summary
	//================================================================================
	def buildSummary() {
	   	var s = "";
 		
	   	processedSentences.toList map { sentence =>
	   	  var newSentence = sentence.grabValue.trim();
	   	  newSentence = newSentence.substring(0, 1).toUpperCase() + newSentence.substring(1);
	   	  s += newSentence + " ";
	   	}
	   	
 		// Last minute linting
 		s = s.replaceAll("``", "\"");
 		s = s.replaceAll("''", "\"");
 		s = s.trim();
 		s = s.substring(0, 1).toUpperCase() + s.substring(1);
 		s = s.replaceAll("-LRB-", "");
 		s = s.replaceAll("-RRB-", "");
 		s = s.replaceAll(" # ", " ");
 		
 		summary = s.trim();
	}
	
	//================================================================================
	// Remove unwanted indices
	//================================================================================
	def removeUnusedSentences(indices:ArrayList[Int]) {
		originalSentences = extractIndicies(indices, originalSentences);
		processedSentences = extractIndicies(indices, processedSentences);
	}
	
	def extractIndicies(indices:ArrayList[Int], sentences:ArrayList[NLPSentence]): ArrayList[NLPSentence] = {
		var newSentences = new ArrayList[NLPSentence]();
		
		for (i <- 0 to sentences.size() - 1) {
			if (indices.contains(i)) { 
			  newSentences.add(sentences.get(i))
			}
		}
		
		return newSentences;
	}
}