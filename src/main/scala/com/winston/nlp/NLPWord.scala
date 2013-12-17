package com.winston.nlp

import com.winston.nlp.transport.messages.TransportMessage

case class NLPWord() extends Ordered[NLPWord] with TransportMessage {
	var value:String = null
	var tf:Long = 0
	var tfidf:Double = 0
	var invalid:Boolean = false
	var startIndex:Int = 0
	var endIndex:Int = 0
	var remove:Boolean = false
	var originalText:String = null
	
	def this(w:String){
	  this()
	  value = w
	}
	
	def grabValue():String = return value
	
	def lowerval = value.toLowerCase();
	
	def setFrequency(l:Long) {
		tf = l;
	}
	
	def copy():NLPWord = {
	  
	  var newWord:NLPWord = new NLPWord(value)
	  newWord.tf = tf
	  newWord.tfidf = tfidf
	  newWord.invalid = invalid
	  newWord.startIndex = startIndex
	  newWord.endIndex = endIndex
	  newWord.originalText = originalText
	  
	  return newWord
	}
	
	def determineValid():Boolean = {
	  var newVal = value.replaceAll("\\W", "DELETE_INVALID_WORD")
	  
	  if(newVal.equals("DELETE_INVALID_WORD")){
	    return false
	  }
	  
	  return true
	}
	
	def determineStopword():Boolean = return invalid
	
	def markInvalid(){
	  invalid = true  
	}
	
	override def toString():String = {
	  var s:String = ""
	  s += value	
	  return s
	}
	
	def compare(other:NLPWord) = (this.tfidf - other.tfidf).toInt
	
}