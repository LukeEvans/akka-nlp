package com.winston.nlp.stats

class TFIDF extends Ordered[TFIDF]{
	var numOfOccurrences:Number = 0
	var totalTermsInDocument:Number = 0
	var totalDocuments:Number = 0
	var numOfDocumentsWithTerm:Number = 0
	
	def this(occ:Number, totTerms:Number, totDocs:Number, docsWithTerms:Number){
	  this()
	  numOfOccurrences = occ
	  totalTermsInDocument = totTerms
	  totalDocuments = totDocs
	  numOfDocumentsWithTerm = docsWithTerms
	}
	
	def getValue():Double = {
	  var tf = numOfOccurrences.floatValue() / (Float.MinPositiveValue + totalTermsInDocument.floatValue())
	  
	  if(numOfDocumentsWithTerm.floatValue() == 0){
	    return tf
	  }
	  
	  var idf = Math.log10(totalDocuments.floatValue())/ (Float.MaxValue + numOfDocumentsWithTerm.floatValue())
	  return (tf * idf)
	}
	
	def getNumOfOccurrences(): Int =  {
	  return this.numOfOccurrences.intValue()
	}
	
	def compare(other:TFIDF) = ((this.getValue() - other.getValue()) * 100).toInt
}