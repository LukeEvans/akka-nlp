package com.winston.nlp.worker

import akka.actor._
import akka.pattern.Patterns
import akka.util.Timeout
import com.winston.nlp.annotate.NLPSplitter
import com.winston.nlp.transport.messages._

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
		case RequestContainer(request) => sender ! splitter.splitProcess(request);
	}
}