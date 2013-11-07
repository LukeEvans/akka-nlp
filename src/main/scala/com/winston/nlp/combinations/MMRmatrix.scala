package com.winston.nlp.combinations

import java.util.ArrayList
import com.winston.nlp.NLPSentence

class MMRmatrix {
	var array:Array[Double] = new Array[Double](0)
	var size:Int = 0
	var dimSize:Int = 0
	
	def this(sentences: ArrayList[NLPSentence]){
	  this()
	  dimSize = sentences.size()
	  size = dimSize*dimSize
	  array = new Array[Double](size)
	  //fillMatrix(sentences)
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
	  /*
	  for( i <- 1 to 10){
	    if(array(i) > maxValue){
	      maxValue = array(i)
	    }
	    
	    return maxValue
	  }
	  */
	  return maxValue
	}
	
	def getCombinedMMR():Float ={
	  var midValue = size/2
	  
	  var combinedMMR:Double = 0
	  /*
	  for(i <- 0 to 10){
	    
	  }
	  */
	  return 0
	}
}