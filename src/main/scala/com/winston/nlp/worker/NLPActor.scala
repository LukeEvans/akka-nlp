package com.winston.nlp.worker

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.winston.nlp.messages._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.winston.nlp.nlp.SentenceSet
import scala.collection.JavaConversions._


class NLPActor(splitRouter:ActorRef, parseRouter:ActorRef) extends Actor { 

	def receive = {
		case raw_text: RawText => process(raw_text, sender);
	}

	// Process Raw Text
	def process(rawText: RawText, origin: ActorRef) {
		implicit val timeout = Timeout(5 seconds);

		val futureSplit = splitRouter ? rawText; 
		val splitSet = Await.result(futureSplit, timeout.duration).asInstanceOf[SetContainer];

		var set = splitSet.set;
		
		val parseFutures: List[Future[SentenceContainer]] = set.sentences.toList map { sentence =>
			ask(parseRouter, SentenceContainer(sentence)).mapTo[SentenceContainer]
		}
		
		val parsed = Await.result(Future.sequence(parseFutures), timeout.duration) 
		
		parsed map { sc =>
			set.replaceSentence(sc.sentence);
		}
		
		origin ! SetContainer(set)
	}
}