package com.winston.nlp.scoring

import akka.actor.Actor
import com.winston.nlp.messages.SentenceContainer
import com.winston.nlp.nlp.NLPSentence
import akka.actor.ActorRef

class SentenceScoringActor extends Actor {
  	
	def receive = {
		case s: SentenceContainer => processScore(s.sentence, sender);
	}
  	
  	def processScore(sentence:NLPSentence, origin:ActorRef) {
  		
  	}
}