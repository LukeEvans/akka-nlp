package com.winston.nlp.scoring

import akka.actor.Actor
import com.winston.nlp.messages.ScoreRequest
import com.winston.nlp.messages.ScoreRequest
import com.winston.nlp.messages.ScoreRespones

class ScoringActor extends Actor {

  	def receive = {
		case score_request: ScoreRequest => sender ! processScore(score_request);
	}
  	
  	// Process Score request
  	def processScore(request:ScoreRequest) : ScoreRespones = {
  	  
  	  null;
  	} 
}