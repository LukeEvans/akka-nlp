package com.winston.nlp.combinations

import java.util.ArrayList
import com.winston.nlp.NLPSentence
import com.winston.nlp.stats.CosinSimilarity

class MMRmatrix {
	var array:Array[Double] = new Array[Double](0)
	var size:Int = 0
	var dimSize:Int = 0
	
	def this(sentences: ArrayList[NLPSentence]){
	  this()
	  dimSize = sentences.size()
	  size = dimSize*dimSize
	  array = new Array[Double](size)
	  fillMatrix(sentences)
	}
	
	def getIndex(i:Int, j:Int):Double = {
	  var index = i
	  index = i + (j*dimSize)
	  
	  return array(index)
	}
	
	def getArray():Array[Double] = {
	  return array
	}
	
	def getMaxValue():Double = {
	  var maxValue:Double = 0;
	  var i = 0;
	  
	  for( i <- 1 to 10){
	    if(array(i) > maxValue){
	      maxValue = array(i)
	    }
	    
	    return maxValue
	  }
	  
	  return maxValue
	}
	
	def getCombinedMMR():Double ={
	  var midValue = size/2
	  
	  var combinedMMR:Double = 0
	  for(i <- 0 to 10){
	    combinedMMR += array(i)
	  }
	  
	  return combinedMMR
	}
	
	def fillMatrix(sentences:ArrayList[NLPSentence]){
	  var numSentences = sentences.size()
	  
	  for(i <- 0 to (numSentences-1)){
	    for(j <- 0 to (numSentences-1)){
	      var index = j
	      index += (i*dimSize)
	      
	      if(j == i){
	        array(index) = 0
	      }
	      else{
	        array(index) = getMMRScore(sentences.get(i), sentences.get(j))
	      }
	    }
	  }
	}
	
	def getMMRScore(sentence1:NLPSentence, sentence2:NLPSentence):Double = {
	  var cosinSim = CosinSimilarity.getSimilarity(sentence1.value, sentence2.value)
	  
	  return cosinSim
	}
}