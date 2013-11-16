package com.winston.nlp

import java.util.ArrayList
import java.util.LinkedHashMap
import com.winston.nlp.messages.TransportMessage
import scala.collection.JavaConversions._
import org.apache.commons.lang.StringUtils
import com.winston.nlp.stats.TFIDF

class SentenceSet(h:String, ft:String) extends TransportMessage {
	var headline:String = h;
	var querySet:ArrayList[String] = new ArrayList[String]
	var totalDocuments:Long = 0
	var totalNumberOfTerms:Int = 0
	var sentences:ArrayList[NLPSentence] = new ArrayList[NLPSentence]
	var fullText:String = ft;
	var countMap:LinkedHashMap[String, Integer] = new LinkedHashMap[String, Integer]
	var headlineWords: ArrayList[String] = _;
	var stopPhrases: ArrayList[String] = _;
	var decay: Boolean = true;
	
	//================================================================================
	// Add sentence 
	//================================================================================
	def addSentence(sentence:NLPSentence) {
	  sentence.index = sentences.size();
	  sentences.add(sentence);
	}
	
	//================================================================================
	// Replace sentence
	//================================================================================
	def replaceSentence(sentence:NLPSentence) {
	  if (sentence.index < sentences.size()) {
	    sentences.set(sentence.index, sentence);
	  }
	}
	
	//================================================================================
	// Mark all words invalid if they're in the stop list
	//================================================================================
	def markInavlidWords(stopPhrases:ArrayList[String]) {
		sentences.toList map { sentence =>
		  sentence.words.toList map { word =>
		    if (stopPhrases.contains(word.value.toLowerCase())) {
		      word.markInvalid;
		    }
		  }
		}
	}
	
	//================================================================================
	// Find total count  
	//================================================================================
	def findTotalObservedCounts() {
	  
		sentences.toList map { sentence =>
		  sentence.words.toList map { word =>
		    if (word.determineValid) {
		      val lowerVal = word.value.toLowerCase();
		      
		      	// If we haven't accounted for the word yet, add all occurrences
				if (!countMap.containsKey(lowerVal)) {
					val occurences = StringUtils.countMatches(fullText.toLowerCase(), lowerVal);
					countMap.put(lowerVal, new Integer(occurences));
				}
		      
		    }
		  }
		}
	}
	
	//================================================================================
	// Find Total terms 
	//================================================================================
	def findTotalTermsInDoc() {
	  	var total = 0;

		// Guard
		if (countMap == null) {
			return;
		}

		for (entry <- countMap.entrySet()) {
			total += entry.getValue();
		}
		
		totalNumberOfTerms = total;
	}
	
	//================================================================================
	// Calculate cosine similarity
	//================================================================================
	def calculateCosinSim() {
		sentences.toList map { sentence =>
			sentence.calculateSimilarity(headline)
		}
	}
	
	//================================================================================
	// Calculate tfidf
	//================================================================================
	def calculateTFIDF() {
	  
	  sentences.toList map { sentence =>
	    sentence.words.toList map { word =>
	    	if (!word.determineStopword) {
	    	  if (countMap.containsKey(word.lowerval)) {
	    	    val tfidf = new TFIDF(countMap.get(word.lowerval), totalNumberOfTerms, totalDocuments, word.tf);
	    	    word.tfidf = tfidf.getValue;
	    	    
	    	    sentence.cummulative_tfidf += word.tfidf;
	    	  }
	    	}
	    }
	  }
	}
	
	//================================================================================
	// Set total doc count
	//================================================================================
	def putTotalCount(totalDocs:Long) {
	  totalDocuments = totalDocs;
	}
	
	//================================================================================
	// Add all word frequencies 
	//================================================================================
	def addWordFrequencies(wordMap: Map[String, Long]) {
		sentences.toList map { sentence =>
	    	sentence.words.toList map { word =>
	    		if (wordMap.contains(word.value)) {
	    		  word.setFrequency(wordMap.apply(word.value));
	    		}
	    	}
		}
	}
	
	//================================================================================
	// Calculate weight
	//================================================================================
	def calculateWeight() {
		var i=1;
		sentences.toList map { sentence => 
		  sentence.caluculateWeight(i, decay);
		  i += 1;
		}
	}
	
	//================================================================================
	// Set decay 
	//================================================================================
	def setDecay(d:Boolean) {
	  decay = d;
	}
}