package com.winston.nlp

import java.util.ArrayList
import java.util.LinkedHashMap

class SentenceSet {
	var headline:String = ""
	var querySet:ArrayList[String] = new ArrayList[String]
	var totalDocuments:Int = 0
	var totalNumberOfTerms:Int = 0
	var sentences:ArrayList[NLPSentence] = new ArrayList[NLPSentence]
	var fullText:String = ""
	var countMap:LinkedHashMap[String, Integer] = new LinkedHashMap[String, Integer]
	var headlineTFIDFs: NLPWord = new NLPWord()
	var headlineWords:String = ""
	var stopPHrases: String = ""
}