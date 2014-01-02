package com.winston.nlp.postProcessing

import java.util.ArrayList
import com.winston.nlp.NLPSentence
import edu.stanford.nlp.trees.Tree
import scala.collection.JavaConversions._

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
		
		return new NLPSentence(tree, sentence);
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
}