package com.winston.nlp

import scala.collection.JavaConversions._
import java.util.ArrayList
import edu.stanford.nlp.trees.Tree
import com.winston.utlities.Tools

class NLPSentence {
	var value:String = null
	var words:ArrayList[NLPWord] = new ArrayList[NLPWord]
	var cosine_score:Double = 0
	var cummulative_tfidf:Double = 0
	var predecayed_weight:Double = 0
	var weight:Double = 0
	//var tree:Tree
	
	def this(s:String, buildWords:Boolean){
	  this()
	  value = s
	  words = new ArrayList[NLPWord]
	}
	
	def this(s:String){
	  this()
	  value = s
	  words = new ArrayList[NLPWord]
	  
	  for(w <- s.split("\\s")){
	    addWord(w)
	  }
	}
	
	def this(t:Tree){
	  this()
	  //value = Tools.getStringFromTree(t)
	  //tree = t.deepCopy()
	  words = new ArrayList[NLPWord]
	  for(w <- value.split("\\s")){
	    addWord(w)
	  }
	}
	
	def addWord(w:String){
	  if(!words.contains(w)){
		  words.add(new NLPWord(w))
	  }
	}
	
}