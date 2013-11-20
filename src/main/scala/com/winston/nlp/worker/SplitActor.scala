package com.winston.nlp.worker

import akka.actor._
import akka.pattern.Patterns
import akka.util.Timeout
import com.winston.nlp.annotate.NLPSplitter
import com.winston.nlp.messages.RawText
import com.winston.nlp.messages.InitRequest
import com.winston.nlp.messages.InitRequest

class SplitActor extends Actor {

	val splitter = new NLPSplitter();

	override def preStart() {
	  println("--Creating splitter");
      self ! InitRequest
	}

	override def postStop() {
		println("--Stopped splitter");
	}
	
	def receive = {
	  	case InitRequest => splitter.init(); 
		case raw_text: RawText => sender ! splitter.splitProcess(raw_text);
	}
}