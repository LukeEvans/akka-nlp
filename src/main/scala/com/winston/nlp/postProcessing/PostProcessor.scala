package com.winston.nlp.postProcessing

import com.winston.nlp.combinations.SentenceCombination
import java.util.ArrayList
import com.winston.nlp.NLPSentence
import com.winston.nlp.SentenceSet
import com.winston.nlp.SummaryResult
import scala.collection.JavaConversions._

class PostProcessor {
	var indices:ArrayList[Int] = _;
	var missingIndices:ArrayList[Int] = _;
	var originalSentences:ArrayList[NLPSentence] = _;
	var processedSentences:ArrayList[NLPSentence]= _;
	var combo:SentenceCombination= _;
	var headline:String = null;

	//================================================================================
	// Constructors
	//================================================================================
	def this(i:ArrayList[Int], s:ArrayList[NLPSentence]) {
		this();
		indices = i;
		missingIndices = new ArrayList[Int]();
		originalSentences = s;
		processedSentences = new ArrayList[NLPSentence]();

		// Start our processed sentences out with the original sentences
		initiateProcessedSentences();
	}

	def this(c:SentenceCombination, set:SentenceSet){
		this();
	  
		combo = c;

		if (combo != null) {
			indices = combo.sentenceNumbers;
		}

		missingIndices = new ArrayList[Int]();

		originalSentences = new ArrayList[NLPSentence]();
		processedSentences = new ArrayList[NLPSentence]();

		if (set != null) { 
			originalSentences = set.sentences;
		}

		headline = set.headline;

		initiateProcessedSentences();
	}

	//================================================================================
	// Process
	//================================================================================
	def process(): SummaryResult = {

		// Determine missing indices
		findMissingIndices();

		// Global tree surgeries
		globalTrim();

		// Gap trimming based on rules
		gapTrim();
		

		val results = new SummaryResult(originalSentences, processedSentences, indices);
		return calculateResultStatistics(results);
	}

	//================================================================================
	// Calculate Return fields
	//================================================================================
	def calculateResultStatistics(result:SummaryResult): SummaryResult = {
		// Headline
		result.article_headline = headline;

		// Saliency and relevance Score
		var saliency:Float = 0;
		var relevance:Float = 0;
		originalSentences.toList map { sentence =>
		  saliency += sentence.cummulative_tfidf.floatValue();
		  relevance += sentence.cosine_score.floatValue();
		}
		result.saliency_score = saliency;
		result.relevance_score = relevance;
		
		// Novelty
		result.novelty_score = combo.getCombinedMMR().floatValue();

		// Social Salience
		result.social_salience = 0;

		return result;
	}
	
	//================================================================================
	// Determine missing indices
	//================================================================================
	def findMissingIndices() {
		for (i <- 0 to originalSentences.size()-1) {
			if (indexOf(i) < 0) {
			  missingIndices.add(i)
			}
		}
	}

	//================================================================================
	// Find location of index in list
	//================================================================================
	def indexOf(item:Int): Int = {
		for (i <- 0 to indices.size()-1) {
		  if (indices.get(i) == item) {
		    return i;
		  }
		}
		
		return -1;
	}

	//================================================================================
	// Copy sentences to processed list to get the ball rolling
	//================================================================================
	def initiateProcessedSentences() {
		originalSentences.toList map { sentence => processedSentences.add(sentence.copy)}
	}

	//================================================================================
	// Trim trees based on rules surrounding gaps
	//================================================================================
	def gapTrim() {
		if (missingIndices == null || missingIndices.size() == 0) {
			return;
		}

		// Find list of sentences that have a missing node preceding it
		val strandedIndices = findStrandedIndices();
		val gapProcessor = new GapRuleProcessor(strandedIndices, processedSentences, indices);
		processedSentences = gapProcessor.process();
	}

	//================================================================================
	// Get all sentences that need to be checked
	//================================================================================
	def findStrandedIndices(): ArrayList[Int] = {
		val stranded = new ArrayList[Int]();

		indices.toList map { index =>
		  val previous = index - 1;
		  
		  if (missingIndices.contains(previous)) {
		    stranded.add(index)
		  }
		}

		return stranded;
	}


	//================================================================================
	// Trim trees regardless of gaps
	//================================================================================
	def globalTrim() {
		val globalProcessor = new GlobalRuleProcessor(processedSentences, indices);
		processedSentences = globalProcessor.process();
	}
}