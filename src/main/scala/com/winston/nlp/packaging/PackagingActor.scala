package com.winston.nlp.packaging

import akka.actor.Actor
import com.winston.nlp.SentenceSet
import com.winston.nlp.transport.messages._
import akka.actor.ActorRef
import com.winston.nlp.combinations.SentenceCombinations
import com.winston.nlp.postProcessing.PostProcessor
import com.winston.nlp.transport.ReductoResponse
import akka.actor.actorRef2Scala
import com.winston.nlp.MasterWorker.MasterWorkerProtocol._

class PackagingActor(manager: ActorRef) extends Actor {
	def receive = {
		case set: SetContainer =>
		  val origin = sender;
		  processPackage(set.set, origin);
	}

	def processPackage(set:SentenceSet, origin:ActorRef) {
	  
		// Get highest combo
		val combos = new SentenceCombinations(set.sentences);
		val combo = combos.getHighestCombo(3, true);
		
		val processor = new PostProcessor(combo, set)
		val result = processor.process;
		
		origin.tell(ResponseContainer(new ReductoResponse(result)), manager)
		manager ! WorkComplete("Done")
	}
}