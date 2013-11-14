package com.winston.nlp.worker

import akka.actor._
import com.winston.nlp.SentenceSet
import com.winston.nlp.combinations.SentenceCombinations

class ComboActor extends Actor {
	println("Combo actors created")

	def receive = {
    	case set:SentenceSet =>{
    		var combos = new SentenceCombinations(set.sentences)
    		var highestCombo = combos.getHighestCombo(3, true)
    		// Send back highest combo
    		sender ! highestCombo
    	}
    	case other:Any =>{
    		println("Message Received not SentenceSet")
    		println("Object type: "+ other.getClass)
    		// Send Error message
    		sender ! "Error"
    	}
	}
}