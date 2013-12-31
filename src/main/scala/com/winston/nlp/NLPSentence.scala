package com.winston.nlp

import scala.collection.JavaConversions._
import java.util.ArrayList
import edu.stanford.nlp.trees.Tree
import com.winston.utlities.Tools
import com.winston.nlp.stats.CosinSimilarity
import com.winston.nlp.transport.messages.TransportMessage

class NLPSentence extends TransportMessage {
	var value:String = null
	var index = -1;
	var words:ArrayList[NLPWord] = new ArrayList[NLPWord]
	var cosine_score:Double = 0
	var cummulative_tfidf:Double = 0
	var predecayed_weight:Double = 0
	var weight:Double = 0
	var treeString:String = null;
	var parseInfo:String = null;
	
	def this(s:String, buildWords:Boolean){
	  this()
	  value = s
	  
	  if (buildWords) {
	  	for(w <- s.split("\\s")){
	  		addWord(w)
	  	}
	  }
	}
	
	def this(s:String, w:ArrayList[NLPWord]){
	  this()
	  value = s;
	  words = w;
	}
	
	def this(s:String){
	  this()
	  value = s
	  words = new ArrayList[NLPWord]
	}
	
	def this(s:String, i:Int){
	  this()
	  value = s
	  index = i
	  words = new ArrayList[NLPWord]
	}
		
	def this(t:Tree, original:NLPSentence){
	  this()
	  value = Tools.getStringFromTree(t)
	  treeString = t.toString();
	  words = new ArrayList[NLPWord]
	  
	  index = original.index;
	  cosine_score = original.cosine_score;
	  cummulative_tfidf = original.cummulative_tfidf;
	  predecayed_weight = original.predecayed_weight;
	  weight = original.weight
	}
	
	def addWord(w:String){
	  if(!words.contains(w)){
		  words.add(new NLPWord(w))
	  }
	}
	

	def addWord(w:String, start:Int, end:Int, originalText:String){
		if(!words.contains(w)){
			var word = new NLPWord(w)
			word.startIndex = start
			word.endIndex = end
			word.originalText = originalText
			words.add(word)
     	}
	}
	
	def grabValue():String = {
	  return value
	}
	
	def grabWords():ArrayList[NLPWord] = {
	  return words
	}
	
	def grabTree():Tree = {
	  if (treeString == null) {
	    println("null treestring")
	    return Tree.valueOf("")
	  }
	  
	  val tree:Tree = Tree.valueOf(treeString);
	  return tree
	}
	
	def putTree(t:String) {
	  val translated = t.replaceAll("\\(TOP", "\\(ROOT")
	  treeString = translated;
	}
	
	def caluculateWeight(location:Int, decay:Boolean){
	  cosine_score *= 1
	  cummulative_tfidf *= 3
	  
	  if(cosine_score == 0 && cummulative_tfidf > 0){
	    weight = cummulative_tfidf
	  }
	  else if (cosine_score > 0 && cummulative_tfidf == 0){
	    weight = cosine_score
	  }
	  else {
	    weight = (cosine_score + cummulative_tfidf)
	  }
	  predecayed_weight = weight
	  
	  if (decay) {
	    decayWeight(location)
	  }
	}
	
	def decayWeight(loc:Int){
	  
	  loc match {
	    case some if some > 1 => {
	      var location = loc
	      var decayFactor:Double = Math.log(location.doubleValue())
	      if(loc == 2){
	        weight *= decayFactor
	        return
	      }   
	      weight /= decayFactor
	    }
	    case _ => return
	  }
	}
	
	def calculateSimilarity(value:String, query:String):Double = {
	  def score = CosinSimilarity.getSimilarity(query, value)
	  return score
	}
	
	def calculateSimilarity(headline:String){
	  var total:Double = 0;
	  
	  total += calculateSimilarity(value, headline)
	  
	  cosine_score = total
	}
	
	def removeIgnoreWords(ignoreWords:ArrayList[String]):String = {
	  var newString = new String()
	  
	  for(word <- words){
	    if(!ignoreWords.contains(word.grabValue().toLowerCase())){
	      if(!word.invalid){
	        newString += word.grabValue() + " "
	      }
	    }
	  }
	  return newString
	}
	
	def addTF(wordValue:String, tf:Long){
	  for(word <- words){
	    if(word.value.equalsIgnoreCase(wordValue)){
	      word.tf = tf
	    }
	  }
	}
	
	def copy():NLPSentence = {
	  var newSentence = new NLPSentence(value, false)
	  newSentence.cosine_score = cosine_score
	  newSentence.cummulative_tfidf = cummulative_tfidf
	  newSentence.index = index
	  newSentence.treeString = treeString
	  newSentence.weight = weight
	  newSentence.parseInfo = parseInfo
	  
	  for( word <- words){
	    newSentence.words.add(word.copy())
	  }
	  
	  return newSentence
	}
	
	def determineValid():Boolean = {
	  return value.length() >= 3
	}
	
	override def toString():String = {
	  var s = ""
	  s += value
	  return s
	}
}