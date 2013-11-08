package com.winston.nlp.worker

import akka.actor._
import akka.pattern.Patterns
import akka.util.Timeout
import com.winston.nlp.annotate.NLPSplitter
import com.winston.nlp.messages.RawText

class SplitActor extends Actor {

	println("--Creating splitter");
	val splitter = new NLPSplitter();

	def receive = {
		case raw_text: RawText => sender ! splitter.splitProcess(raw_text);
	}
}