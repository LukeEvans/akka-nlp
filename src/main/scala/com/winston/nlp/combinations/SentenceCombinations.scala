package com.winston.nlp.combinations

import scala.collection.JavaConversions._
import java.util.ArrayList
import com.winston.nlp.NLPSentence
import com.winston.nlp.validation.Validator
import scala.util.Sorting

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
	
	def this(nlpSentences:ArrayList[NLPSentence], separationMax:Int){
	  this()
	  
	  sentences = new ArrayList[String]
	  scores = new ArrayList[Double]
	  
	  this.nlpSentences = nlpSentences
	  this.maxSentSeparation = separationMax
	  
	  for(sentence <- nlpSentences){
	    sentences.add(sentence.value)
	    scores.add(sentence.weight)
	  }
	  
	  size = sentences.size()
	  
	}
	
	def getHighestCombo(numSentences:Int, sentence:Boolean):SentenceCombination = {
	  var combos:ArrayList[SentenceCombination] = generateCombinations(numSentences, sentence)

	  return findHighestMMRCombo(scores, combos)
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
	
	def findHighestMMRCombo(scores: ArrayList[Double], combos: ArrayList[SentenceCombination]): SentenceCombination = {
	  var combosForRemoval = new ArrayList[SentenceCombination];
          
	  for(combo <- combos){
		  if(combo.isOverMaxMMR())
			  combosForRemoval.add(combo)
      }
            
	  for(combo <- combosForRemoval){
        	  combos.remove(combo)
	  }  
          
	  for(combo <- combos){
		  combo.mmrScore = getScore(scores, combo.sentenceNumbers)
	  }
          
	  var sorted = combos.sortWith((x,y) => x.mmrScore > y.mmrScore)
          
	  for(combo <- sorted){
		  val validator = new Validator(combo.sentenceNumbers, nlpSentences);
		  if(validator.validate()){
			  return combo
		  }
      }
	  return sorted.get(0);
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
	    for(i <- min to (max - 1)){
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
	    for(i <- 1 to (listSize-1)){
	      if(getDifference(list.get(i), list.get(i-1)) > difference){
	        return true
	      }
	    }
	  }
	  	return false
	}
	
	def getDifference(a:Int, b:Int):Int= {
	  val diff = Math.abs(a - b);
	  diff;
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