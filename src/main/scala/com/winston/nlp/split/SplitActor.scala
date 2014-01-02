package com.winston.split

import akka.actor._
import com.winston.nlp.transport.messages._
import com.winston.nlp.MasterWorker.MasterWorkerProtocol._
import com.winston.nlp.split.NLPSplitter

class SplitActor(manager: ActorRef) extends Actor {

	val splitter = new NLPSplitter();

	override def preStart() {
	  println("--Creating splitter");
      self ! InitRequest
	}

	override def postStop() {
		println("--Stopped splitter");
	}
	
	def receive = {
	  	case InitRequest => 
	  	  splitter.init()
	  	  manager ! ReadyForWork
		case RequestContainer(request) => 
		  sender.tell(splitter.splitProcess(request), manager)
		  manager ! WorkComplete("Done")
	}
}