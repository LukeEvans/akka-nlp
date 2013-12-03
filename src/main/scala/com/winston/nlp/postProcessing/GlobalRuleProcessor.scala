package com.winston.nlp.postProcessing

import java.util.ArrayList
import com.winston.nlp.NLPSentence
import com.winston.nlp.NLPWord
import edu.stanford.nlp.trees.Tree
import scala.collection.JavaConversions._
import com.winston.utlities.Tools
import com.winston.nlp.NLPSentence

class GlobalRuleProcessor extends TreeProcessor {

	var sentences:ArrayList[NLPSentence] = _;
	var indicesIncluded:ArrayList[Int] = _;

	//================================================================================
	// Constructors
	//================================================================================
	def this(s:ArrayList[NLPSentence], ii:ArrayList[Int]) {
		this();
		sentences = s;
		indicesIncluded = ii;
	}

	//================================================================================
	// Process
	//================================================================================
	def process():ArrayList[NLPSentence] = {
		var newSentences = new ArrayList[NLPSentence]();
		
		var i=0;

		sentences.toList map { sentence =>
			if (!indicesIncluded.contains(i)) {
			  newSentences.add(sentence);
			}
			
			else {
			  newSentences.add(lintSentence(i, sentence));
			}
			
			i += 1;
		}
		
		return newSentences;
	}

	//================================================================================
	// Run the rules on a given sentence
	//================================================================================
	def lintSentence(index:Int, sentence:NLPSentence): NLPSentence = {
		if (sentence == null || sentence.grabTree() == null) {
			return sentence;
		}

		var tree = sentence.grabTree().deepCopy();

		// Parentheticals
		tree = parantheticals(tree);
		
		// Lead adverbials
		tree = headline(index, tree);

		// Cardinals like ...,27.
		tree = cardinals(tree);
		
		var nlpSentence = reconstructSentence(sentence.words, tree)
		nlpSentence.index = sentence.index
		nlpSentence.treeString = tree.toString()
		return nlpSentence
		//return new NLPSentence(tree, sentence);
	}

	//================================================================================
	// Parentheticals
	//================================================================================
	def parantheticals(t:Tree): Tree = {
		
		var tree = t;
		
		// Remove Parentheticals
		tree = tsurgeonScript(tree, "PRN=prn < -LRB- < -RRB-", "prune prn");
		tree = tsurgeonScript(tree, "LST=lst < -LRB- < -RRB-", "prune lst");
		tree = tsurgeonScript(tree, "PRN=prn < (/:/ $-- /:/)", "prune prn");
		
		return tree;
	}
	
	//================================================================================
	// Lead Adverbials
	//================================================================================
	def headline(index:Int, t:Tree): Tree = {
		var tree = t;
		if (index == 0) {
			tree = tsurgeonScript(tree, 10, "__=leading .. (/^:$/ !,, /^:$/)", "prune leading");
			return tsurgeonScript(tree, "/^:$/=colon >>, ROOT", "prune colon");
		}
		
		return tree;
	}
	
	//================================================================================
	// Single cardinals
	//================================================================================
	def cardinals(t:Tree): Tree = {
		var tree = t;
		tree = tsurgeonScript(tree, "CD=cd , /,/=comma . /\\./", "prune cd comma");

		return tree;
	}

	//================================================================================
	// , Person said. 
	//================================================================================
	def personSaid(t:Tree): Tree = {
		var tree = t;
		tree = tsurgeonScript(tree, 10, "__=del ,, /,/=comma .. (/said/=said . /\\./)", "excise del said", "prune comma said");
		tree = tsurgeonScript(tree, 10, "__=del ,, /,/=comma .. (/reports/=rp . /\\./)", "excise del said", "prune comma rp");
		return tree;
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
		while(j < leaves.size() && i < words.size()){
			var leafString = leaves.get(j).value
			var wordString = words.get(j).grabValue();
			if(leafString.equalsIgnoreCase(wordString)){
				var word = new NLPWord(leaves.get(i).toString());
				if(j != 0 && words.get(j-1).endIndex == words.get(j).startIndex){
					var endIndex = newWords.get(newWords.size() - 1).endIndex;
					word.startIndex = endIndex;
					word.endIndex = words.get(j).endIndex;
				}
				else{
					word.startIndex = words.get(j).startIndex;
					word.endIndex = words.get(j).endIndex;
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