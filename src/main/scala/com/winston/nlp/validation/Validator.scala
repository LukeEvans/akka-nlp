package com.winston.nlp.validation

import java.util.ArrayList
import com.winston.nlp.NLPSentence
import java.util.ArrayList
import edu.stanford.nlp.trees.tregex.TregexMatcher
import edu.stanford.nlp.trees.tregex.TregexPattern
import edu.stanford.nlp.trees.Tree
import scala.collection.JavaConversions._
import shapeless.ToList
import com.winston.nlp.postProcessing.PostProcessor
import com.winston.nlp.postProcessing.GapRuleProcessor

class Validator {

	var indices:ArrayList[Int] = _;
	var sentences:ArrayList[NLPSentence] = _;
	
	//================================================================================
	// Constructors
	//================================================================================
	def this(i:ArrayList[Int], s:ArrayList[NLPSentence]) {
		this();
		indices = i;
		sentences = s;
	}
	
	//================================================================================
	// Validate
	//================================================================================
	def validate(): Boolean = {
		
	  // Check for global violations
	  extractIndices(indices).toList map { sentence =>
	    if (!globalTreeValid(sentence.grabTree, sentence.grabValue)) {
	      return false;
	    }
	  }
		
		val postProcessor = new PostProcessor(indices, sentences);
		postProcessor.findMissingIndices();
		var strandedIndices = postProcessor.findStrandedIndices();
		
		// Check for gap violations after linting
		val gapLinter = new GapRuleProcessor(strandedIndices, sentences, indices);
		var lintedSentences = gapLinter.process();
		
		extractIndices(lintedSentences, strandedIndices).toList map { sentence =>
		  	if (!gapTreeValid(sentence.grabTree())) {
				return false;
			}
		}
		
		return true;
	}
	
	//================================================================================
	// Extract list of sentences according to indices
	//================================================================================
	def extractIndices(indexList:ArrayList[Int]): ArrayList[NLPSentence] = {
		return extractIndices(sentences,indexList);
	}
	
	def extractIndices(list:ArrayList[NLPSentence], indexList:ArrayList[Int]): ArrayList[NLPSentence] = {
		var newsSentences = new ArrayList[NLPSentence]();
		
		for (i <- 0 to list.size()-1) {
			if (indexList.contains(i)) {
			  newsSentences.add(list.get(i));
			}
		}
		
		return newsSentences;
	}
	
	//================================================================================
	// Determine if tree is invalid globally
	//================================================================================
	def globalTreeValid(tree:Tree, original:String): Boolean = {
		
		// If tree is null, let it through
		if (tree == null) {
			return true;
		}
		
		// Only left quote
		if (ruleMatches(tree, "/``/ !.. /''/")) {
			if (original.contains("\"")) {
				return false;
			}
		}
		
		// Only right quote
		if (ruleMatches(tree, "/''/ !,, /``/")) {
			if (original.contains("\"")) {
				return false;
			}
		}
		
		// Whole tree is a parenthetical
		if (ruleMatches(tree, "-LRB-=lrb >>, ROOT .. (-RRB-=rrb >>- ROOT)")) {
			return false;
		}
		
		return true;
	}
	
	//================================================================================
	// Determine if tree is valid on a gap basis
	//================================================================================
	def gapTreeValid(tree:Tree): Boolean = {
		// If tree is null, say it's valid
		if (tree == null) {
			return true;
		}
		
		// Also, in the wrong spot
		if (ruleMatches(tree, "/also/ >> (__ >>, ROOT $++ __)")) {
			return false;
		}
		
		// Pronoun before NN*
		if (ruleMatches(tree, "PRP !,, @/NN.?/")) {
			return false;
		}
		
		// Literals
		if (ruleMatches(tree, "NP < (DT [. /trio/ | ./duo/]) >>, ROOT")) {
			return false;
		}

		
		return true;
	}
	
	//================================================================================
	// Determine if rule has any matches
	//================================================================================
	def ruleMatches(tree:Tree, rule:String): Boolean = {
		var trees = tregexRule(tree, rule);
		
		if (trees == null || trees.size() == 0) {
			return false;
		}
		
		return true;
	}
	
	//================================================================================
	// Process Tregex rules
	//================================================================================
	def tregexRule(tree:Tree, ruleString:String): ArrayList[Tree] = {
		
		var trees = new ArrayList[Tree]();

		val pattern = TregexPattern.compile(ruleString);
		val matcher = pattern.matcher(tree);

		while (matcher.find()) {
			val newTree = matcher.getMatch();
			trees.add(newTree);
		}

		return trees;
	}
}