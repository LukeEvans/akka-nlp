package com.winston.nlp.worker

import com.winston.nlp.annotate.NLPParser
import com.winston.nlp.messages._;
import akka.actor.Actor

class ParseActor extends Actor {

	println("--Creating Parser");
	val parser = new NLPParser()

	def receive = {
		case sc:SentenceContainer => sender ! parser.parseProcess(sc.sentence)
	}
	
	
}