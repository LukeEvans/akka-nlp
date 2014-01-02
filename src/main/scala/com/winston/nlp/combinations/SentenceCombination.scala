package com.winston.nlp.combinations

import scala.collection.JavaConversions._
import java.util.ArrayList
import com.winston.nlp.NLPSentence

case class SentenceCombination() extends Ordered[SentenceCombination]{
	var size:Int = 0
	var sentenceNumbers:ArrayList[Int] = new ArrayList[Int]
	var sentences:ArrayList[NLPSentence] = new ArrayList[NLPSentence]
	var mmr:MMRmatrix = new MMRmatrix
	var combinedWeight:Double = 0
	var mmrThreshold:Double = 0.5
	var mmrScore:Double = 0.0
	
	def this(sentences: ArrayList[NLPSentence], sentenceNumbers:ArrayList[Int]){
	  this()
	  size = sentences.size()
	  this.sentences = sentences
	  this.sentenceNumbers = sentenceNumbers
	  mmr = new MMRmatrix(sentences);
	  combinedWeight = calcCombined(sentences);
	}
	
	def isOverMaxMMR():Boolean = {
	  if(mmr.getMaxValue() > mmrThreshold){
	    return true
	  }
	  else{
	    return false
	  }
	}
	
	def calcCombined(sents:ArrayList[NLPSentence]):Double = {
	  if(sents == null){
	    return 0
	  }
	  var combined:Double = 0
	  
	  for(sentence <- sents){
	    combined += sentence.weight
	  }
	  
	  return combined
	}
	
	def getCombinedMMR():Double = mmr.getCombinedMMR()
	
	// compare override
	def compare(other: SentenceCombination) = (this.getCombinedMMR - other.getCombinedMMR).toInt
	
	
}