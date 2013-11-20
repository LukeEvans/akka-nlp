package com.winston.nlp.stats

import java.util.ArrayList
import scala.collection.JavaConversions._

object CosinSimilarity {
  
	def getSimilarity(firstString:String, secondString:String):Double = {
	  var first = firstString.toLowerCase()
	  var second = secondString.toLowerCase()
	  
	  var tokens1:ArrayList[String] = getTokens(first)
	  var tokens2:ArrayList[String] = getTokens(second)
	  
	  var uniqueTokens1: ArrayList[String] = getUnique(tokens1)
	  var termsInString1 = uniqueTokens1.size()
	  
	  var uniqueTokens2 = getUnique(tokens2)
	  var termsInString2 = uniqueTokens2.size()
	  
	  var tempTokens = new ArrayList[String]()
	  
	  tempTokens.addAll(tokens1)
	  tempTokens.addAll(tokens2)
	  var uniqueTempTokens = getUnique(tempTokens)
	  var commonTerms = (termsInString1 + termsInString2) - uniqueTempTokens.size()
	  
	  var coSim = (commonTerms / (Math.pow(termsInString1, .5) * Math.pow(termsInString2, .5))).asInstanceOf[Double]  
	  return coSim
	}
	
	def getTokens(entireString:String):ArrayList[String] = {
	  var tokens = new ArrayList[String]()
	  
	  var words = entireString.split("[ ';-]")
	  
	  for(word <- words){
	    tokens.add(word)
	  }
	  
	  return tokens
	}
	
	def getUnique(tokens:ArrayList[String]):ArrayList[String] = {
	  var uniqueTokens = new ArrayList[String]
	  
	  for(token <- tokens){
	    if(!uniqueTokens.contains(token)){
	      uniqueTokens.add(token)
	    }
	  }
	  
	  return uniqueTokens
	}
}