package com.winston.nlp.worker

import com.winston.nlp.annotate.NLPParser
import com.winston.nlp.transport.messages._
import akka.actor.Actor

class ParseActor extends Actor {

	val parser = new NLPParser()

	override def preStart() {
	  println("--Creating Parser");
      self ! InitRequest
	}
	
	override def postStop() {
		println("--Stopped parser");
	}
	
	def receive = {
	  	case InitRequest => parser.init(); 
		case sc:SentenceContainer => sender ! parser.parseProcess(sc.sentence)
	}
	
	
}