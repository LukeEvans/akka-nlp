package com.winston.nlp.postProcessing

import java.util.ArrayList
import com.winston.nlp.NLPSentence
import edu.stanford.nlp.trees.Tree
import scala.collection.JavaConversions._

class GapRuleProcessor extends TreeProcessor {

 	var indicesToCheck:ArrayList[Int] = _;
	var indicesIncluded:ArrayList[Int] = _;
	var sentences:ArrayList[NLPSentence] = _;

	//================================================================================
	// Constructors
	//================================================================================
	def this(i:ArrayList[Int], s:ArrayList[NLPSentence], ii:ArrayList[Int]) {
		this();
		indicesToCheck = i;
		sentences = s;
		indicesIncluded = ii;
	}

	//================================================================================
	// Process
	//================================================================================
	def process(): ArrayList[NLPSentence] = {
		val newSentences = new ArrayList[NLPSentence]();

		var i = 0;
		sentences.toList map { sentence =>
			if (!indicesToCheck.contains(i) || !indicesIncluded.contains(i)) {
			  newSentences.add(sentence);
			} 
			
			else {
			  newSentences.add(lintSentence(sentence))
			}
			
			i += 1;
		}

		return newSentences;
	}

	//================================================================================
	// Run the rules on a given sentence
	//================================================================================
	def lintSentence(sentence:NLPSentence): NLPSentence = {
		if (sentence == null || sentence.grabTree() == null) {
			return sentence;
		}

		var tree = sentence.grabTree().deepCopy();

		// Lead adverbials
		tree = leadAdverbials(tree);

		// Lead prepositional
		tree = prepositional(tree);

		// Gramatical Particles
		tree = gramaticalParticles(tree);
		
		// Dangling Subject
		tree = danglingSubject(tree);
		
		return new NLPSentence(tree, sentence);
	}

	//================================================================================
	// Lead Adverbials
	//================================================================================
	def leadAdverbials(tree:Tree): Tree = {
		return tsurgeonScript(tree, "(ADVP=advp >>, ROOT !< NP) . /,/=comma", "prune advp", "prune comma");
	}

	//================================================================================
	// Lead Prepositional
	//================================================================================
	def prepositional(tree:Tree): Tree = {
		return tsurgeonScript(tree, 10, "PP=pp >>, ROOT . /,/=comma", "prune pp comma");
	}
	
	//================================================================================
	// Gramatical particles
	//================================================================================
	def gramaticalParticles(tree:Tree):Tree = {
		return tsurgeonScript(tree, "IN=in > SBAR >>, ROOT . /^,$/=comma", "prune in comma");
	}
	
	//================================================================================
	// Danging subject
	//================================================================================
	def danglingSubject(tree:Tree): Tree = {
		return tsurgeonScript(tree, 10, "S=sub . /,/=comma  < (__=item !$++ __) < (~item!$-- __) >>, ROOT", "prune sub comma");
	}
}