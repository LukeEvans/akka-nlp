package com.winston.nlp.worker

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.winston.nlp.messages._
import scala.concurrent.duration._
import scala.concurrent.Await


class NLPActor(splitRouter:ActorRef) extends Actor { 

	println("NLP Actor active");
	
	def receive = {
		case raw_text: RawText => process(raw_text);
		case text:Any => println(text) 
	}

	// Process Raw Text
	def process(rawText: RawText) {
		implicit val timeout = Timeout(5 seconds);
		val futureSplit = splitRouter ? rawText; 
		val split = Await.result(futureSplit, timeout.duration).asInstanceOf[SplitSentences]
		
		println(split);
	}
}