package com.winston.nlp.postProcessing

import java.util.ArrayList
import com.winston.nlp.NLPSentence
import edu.stanford.nlp.trees.Tree
import scala.collection.JavaConversions._
import com.winston.utlities.Tools
import com.winston.nlp.NLPWord

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
		
		return newSentences
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
		
		var nlpSentence = reconstructSentence(sentence.words, tree)
		nlpSentence.index = sentence.index
		nlpSentence.treeString = tree.toString()
		return nlpSentence
        //return new NLPSentence(tree, sentence);
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
	
		//================================================================================
	// Reconstruct Sentence
	//================================================================================
	def reconstructSentence(words:ArrayList[NLPWord], tree:Tree):NLPSentence = {
		if(words.isEmpty || words == null || tree == null)
			return null;
	  
		var leaves:java.util.List[Tree] = tree.getLeaves()
		var newTree = tree
		var newWords = new ArrayList[NLPWord]();
		
		var i:Int = 0
		var j:Int = 0;
		while(i < leaves.size() && j < words.size()){
			var leafString = leaves.get(i).value
			var wordString = words.get(j).grabValue();
			if(leafString.equalsIgnoreCase(wordString)){
				var word = new NLPWord(leaves.get(i).toString());
				if(j != 0 && words.get(j-1).endIndex == words.get(j).startIndex){
					var endIndex = newWords.get(newWords.size() - 1).endIndex;
					word.startIndex = endIndex;
					word.endIndex = words.get(j).endIndex;
					word.originalText = words.get(j).originalText
				}
				else{
					word.startIndex = words.get(j).startIndex;
					word.endIndex = words.get(j).endIndex;
					word.originalText = words.get(j).originalText
				}
				newWords.add(word);
				i += 1;
				j += 1;
			}
			else{
				j += 1;
			}
		}
		
		var sentenceString = Tools.getStringFromList(newWords);
		return new NLPSentence(sentenceString, newWords);
	}
}