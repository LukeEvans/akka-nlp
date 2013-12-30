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
  
	manager ! ReadyForWork
  
	def receive = {
	  case pack: PackagingContainer =>
	    val origin = sender
	    processPackage(pack, origin)
	}
	
	def processPackage(pack:PackagingContainer, origin:ActorRef){
	  //Get highest combo
	  var combos:SentenceCombinations = null
	  if(pack.separationRulesOn)
		  combos = new SentenceCombinations(pack.set.sentences, 4)
	  else
		  combos = new SentenceCombinations(pack.set.sentences, pack.set.sentences.size())
	  val processor = new PostProcessor(combos.getHighestCombo(pack.number, true), pack.set)
	  val result = processor.process
	  
	  origin.tell(ResponseContainer(new ReductoResponse(result)), manager)
	  manager ! WorkComplete("Done")
	}
}