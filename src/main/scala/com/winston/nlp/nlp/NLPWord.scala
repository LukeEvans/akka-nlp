package com.winston.nlp.nlp

import com.winston.nlp.messages.TransportMessage

class NLPWord(v:String) extends TransportMessage {
	val value = v;
	var term_frequency = 0.0;
	var tfidf = 0.0;
	var invalid = false;
}