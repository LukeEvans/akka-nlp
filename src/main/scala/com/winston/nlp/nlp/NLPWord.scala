package com.winston.nlp.nlp

class NLPWord(v:String) {
	val value = v;
	var term_frequency = 0.0;
	var tfidf = 0.0;
	var invalid = false;
}