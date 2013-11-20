package com.winston.nlp.postProcessing

import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.trees.tregex.TregexPattern
import java.util.ArrayList
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern
import scala.collection.JavaConversions._
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon
import scala.collection.immutable.Seq

class TreeProcessor {

 	//================================================================================
	// Tree surgeon script with arraylist
	//================================================================================
	def tsurgeonScript(t:Tree, pattern:String, operations:ArrayList[String]): Tree = {
	  	val matchPattern = TregexPattern.compile(pattern);
		val ps = new ArrayList[TsurgeonPattern]();
		
		operations.toList map { op => ps.add(Tsurgeon.parseOperation(op)) }

		val result = Tsurgeon.processPattern(matchPattern, Tsurgeon.collectOperations(ps), t);

		return result;
	}
	
	//================================================================================
	// Tree surgeon script 
	//================================================================================
	def tsurgeonScript(t:Tree, pattern:String, operations:String*): Tree = {
		val opsList = new ArrayList[String];
		operations.toList map { op => opsList.add(op)}
		
		return tsurgeonScript(t, pattern, opsList);
	}

	//================================================================================
	// Tree surgeon script with length limit
	//================================================================================
	def tsurgeonScript(t:Tree, maxToCut:Int, pattern:String, operations:String*): Tree = {
		val opsList = new ArrayList[String];
		operations.toList map { op => opsList.add(op)}
		val newTree = tsurgeonScript(t.deepCopy(), pattern, opsList);
		
		val difference = t.yieldWords().size() - newTree.yieldWords().size();

		if (difference > maxToCut) {
			return t;
		}

		return newTree;
	}
	
}