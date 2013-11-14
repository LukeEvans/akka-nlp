package com.winston.nlp.combinations

import scala.collection.JavaConversions._

import java.util.ArrayList
import com.winston.nlp.NLPSentence

class SentenceCombinations {
	var sentences:ArrayList[String] = new ArrayList[String]
	var nlpSentences:ArrayList[NLPSentence] = new ArrayList[NLPSentence]
	var scores:ArrayList[Double] = new ArrayList[Double]
	var maxSentSeparation:Int = 4
	var size:Int = 20
	var charLimit:Int = 300
	
	def this(size:Int){
	  this()
	  this.size = size
	}
	
	def this(nlpSentences:ArrayList[NLPSentence]){
	  this()
	  sentences = new ArrayList[String]
	  scores = new ArrayList[Double]
	  
	  this.nlpSentences = nlpSentences
	  
	  for(sentence <- nlpSentences){
	    sentences.add(sentence.value)
	    scores.add(sentence.weight)
	  }
	  
	  size = sentences.size()
	  
	}

	def getHighestIntCombo(numSentences:Int, sentence:Boolean): ArrayList[Int] = {
	  var combos:ArrayList[SentenceCombination] = generateCombinations(numSentences, sentence)
	  
	  // sort the combos 
	  
	  var newCombos:ArrayList[SentenceCombination] = new ArrayList[SentenceCombination]
	  
	  for(combo <- combos){
	    // Run validation actors
	    // add to new Combos if valid
	  }
	  
	  var sentenceSet:ArrayList[Int] = findHighestMMRIntegerCombo(scores, newCombos)
	  
	  return sentenceSet
	}
	
	def getHighestCombo(numSentences:Int, sentence:Boolean):SentenceCombination = {
	  var combos:ArrayList[SentenceCombination] = generateCombinations(numSentences, sentence)
	  
	  // Sort the combos
	  
	  var newCombos:ArrayList[SentenceCombination] = new ArrayList[SentenceCombination]
	  
	  for(combo <- combos){
	    // Run validation accotrs
	    // add ot new Combos if valid
	  }
	  
	  return findHighestMMRCombo(scores, newCombos)
	}
		
	def generateCombinations(limit:Int, sentence:Boolean): ArrayList[SentenceCombination] = {
	  var storedList:ArrayList[ArrayList[Int]] = new ArrayList[ArrayList[Int]]
	  var number:ArrayList[Int] = new ArrayList[Int]
	  
	  if(sentence){
	    getCombosSentLimit(number, 0, size, limit, storedList)
	  }
	  else{
	    charLimit = limit
	    getCombosCharLimit(number, 0, size, 15, storedList, nlpSentences)
	  }
	  
	  var combos:ArrayList[SentenceCombination] = new ArrayList[SentenceCombination]
	  
	  for(sentenceNumbers <- storedList){
	    
	    var nlpSents = new ArrayList[NLPSentence]
	    
	    for(index <- sentenceNumbers){
	      nlpSents.add(nlpSentences.get(index))
	    }
	    var combo = new SentenceCombination(nlpSents, sentenceNumbers)
	    
	    combos.add(combo)
	  }
	  
	    return combos
	}
	
	def findHighestMMRIntegerCombo(scores:ArrayList[Double], combos:ArrayList[SentenceCombination]):ArrayList[Int] = {
	  
	  var comboRemoval:ArrayList[SentenceCombination] = new ArrayList[SentenceCombination]
	  
	  for(combo <- combos){
	    if(combo.isOverMaxMMR){
	      println("Removed combo: " + combo.sentenceNumbers)
	      comboRemoval.add(combo)
	    }
	  }
	  
	  for (combo <- comboRemoval){
	    combos.remove(combo)
	  }
	  
	  var highestScore = new ArrayList[Int]
	  
	  for(i <- 0 to combos.get(0).size){
	    highestScore.add(-1)
	  }
	  
	  for(i <- 0 to combos.size()){
	    var score = getScore(scores, combos.get(i).sentenceNumbers)
	    var highScore = getScore(scores, highestScore)
	    if(highScore < score)
	      highestScore = combos.get(i).sentenceNumbers
	  }
	  
	  return highestScore
	}
	
	def findHighestMMRCombo(scores: ArrayList[Double], combos: ArrayList[SentenceCombination]): SentenceCombination = {
	  var combosForRemoval = new ArrayList[SentenceCombination];
	  
	  for(combo <- combos){
	    if(combo.isOverMaxMMR()){
	      combosForRemoval.add(combo)
	    }
	  }
	    
	  for(combo <- combosForRemoval){
		 combos.remove(combo)
	  }
	    
	  var returnCombo = new SentenceCombination()
	    
	  for(i <- 0 to combos.get(0).size){
	     returnCombo.sentenceNumbers.add(-1)
	  }
	    
	  for(i <- 0 to combos.size()){
	    var score = getScore(scores, combos.get(i).sentenceNumbers)
	    var highScore = getScore(scores, returnCombo.sentenceNumbers)
	      
	    if(highScore < score){
	      returnCombo = combos.get(i)
	    }
	  }
	  return new SentenceCombination()
	}

	def getCombosSentLimit(numbers:ArrayList[Int], min:Int, max:Int, n:Int, store: ArrayList[ArrayList[Int]]){
	  if(n == 0){
	    if(!tooSeparate(numbers, maxSentSeparation)){
	      //copy
	      var copy:ArrayList[Int] = numbers.clone().asInstanceOf[ArrayList[Int]];
	      store.add(copy)
	    }
	  }
	  else if(n > 0){
	    for(i <- min to max){
	      numbers.add(i)
	      getCombosSentLimit(numbers, i+1, max, n-1, store)
	      numbers.remove(numbers.size() - 1)
	    }
	  }
	}
	
	def getCombosCharLimit(numbers:ArrayList[Int], min:Int, max:Int, n:Int, store: ArrayList[ArrayList[Int]], sentences:ArrayList[NLPSentence]){
	  if(n > 0){
	    for(i <- min to max){
	      numbers.add(i)
	      getCombosCharLimit(numbers, i+1, max, n-1, store, sentences)
	      
	      if(!tooSeparate(numbers, maxSentSeparation)){
	        var sents = new ArrayList[NLPSentence]
	        for(number <- numbers){
	          sents.add(sentences.get(number))
	        }
	        
	        if(comboWithinLimit(sents, charLimit)){
	          var copy:ArrayList[Int] = numbers.clone().asInstanceOf[ArrayList[Int]]
	          store.add(copy)
	        }
	      }
	      numbers.remove(numbers.size() - 1)
	    }
	  }
	}
	
	def tooSeparate(list:ArrayList[Int], difference:Int):Boolean = {
	  var listSize = list.size()
	  if(listSize == 0 || list == null){
	    return true
	  }
	  else if(listSize == 1){
	    return false
	  }
	  else{
	    for(i <- 1 to listSize){
	      if(getDifference(list.get(i), list.get(i-1)) > difference){
	        return true
	      }
	    }
	  }
	  	return false
	}
	
	def getDifference(a:Int, b:Int):Int= {
	  return Math.abs(a -b)
	}
	
	def comboWithinLimit(sentenceList:ArrayList[NLPSentence], limit:Int):Boolean = {
	  if(sentenceList == null){
	    return false
	  }
	  var combinedCharCount = 0
	  
	  for(sentence <- sentenceList){
	    combinedCharCount += sentence.value.length()
	  }
	  
	  if((combinedCharCount < (limit + 50)) && (combinedCharCount > (limit - 50))){
	    return true
	  }
	  else{
	    return false
	  }
	}
	
	def getScore(scores:ArrayList[Double], combo: ArrayList[Int]):Double = {
	  var score:Double = 0
	  for(sentence <- combo){
	    if(sentence < 0){
	      score += 0
	    }
	    else{
	      score +=scores.get(sentence)
	    }
	  }
	  return score
	}
}