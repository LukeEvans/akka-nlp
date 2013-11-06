package com.winston.nlp.annotate

import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

class NLPResponse {
	var status:String = "OK"
	var start:Long = System.currentTimeMillis()
	var sentences:ArrayList[String] = new ArrayList[String]
	var trees:ArrayList[String] = new ArrayList[String]
	
	
	def addSentence(sentence: String){
	  sentences.add(sentence)
	}
	
	def addTree(tree:String){
	  trees.add(tree)
	}
	
	def addTrees(trees:ArrayList[String]){
	  for(tree <- trees){
	    addTree(tree)
	  }
	}
	
//	def stopTimer(){
//	  timer = Duration.create(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS)
//	  duration = timer.toString()
//	}
	
}