package com.winston.nlp.scoring

import akka.actor.Actor
import org.apache.xpath.operations.String

class ScoringActor extends Actor {

  	def receive = {
		case score_request: String => sender ! score_request;
	}
  	
}