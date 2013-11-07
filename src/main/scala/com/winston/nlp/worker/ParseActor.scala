package com.winston.nlp.worker

import com.winston.nlp.annotate.NLPParser
import com.winston.nlp.messages.RawText
import akka.actor.Actor
import com.winston.nlp.messages.RawSentece

class ParseActor extends Actor {

	println("--Creating Parser");
	val parser = new NLPParser()

	def receive = {
		case raw_sentece: RawSentece => sender ! parser.parseProcess(raw_sentece.text);
	}
}