package com.winston.nlp

import java.util.ArrayList
import scala.collection.JavaConversions._
import scala.util.control.Breaks._

class SocialSalience {
	var words:ArrayList[NLPWord] = new ArrayList[NLPWord]
	var salient_terms:ArrayList[String] = new ArrayList[String]
	var salient_score:Double = 0
	
	def this(w: ArrayList[NLPWord]){
	  this()
	  words = w
	  salient_terms = new ArrayList[String]
	  process()
	}
	
	def process(){
	  var max = 3
	  
	  var i =  0
	  
	  for(word <- words){
	    salient_terms.add(word.grabValue())
	    i += 1
	    
	    if(i==max){
	      break
	    }
	  }
	  
	  var total:Double = 0
	  
	  for(word <- words){
	    total += word.tfidf
	  }
	  
	  salient_score = total
	}
}