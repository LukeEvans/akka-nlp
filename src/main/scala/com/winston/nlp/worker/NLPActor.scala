package com.winston.nlp.worker

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.winston.nlp.messages._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class NLPActor(splitRouter:ActorRef, parseRouter:ActorRef) extends Actor { 

	def receive = {
		case raw_text: RawText => process(raw_text, sender);
	}

	// Process Raw Text
	def process(rawText: RawText, origin: ActorRef) {
		implicit val timeout = Timeout(5 seconds);

		val futureSplit = splitRouter ? rawText; 
		val split = Await.result(futureSplit, timeout.duration).asInstanceOf[SplitSentences];

		val parseFutures: List[Future[ParsedSentence]] = split.sentences map { sentence =>
			ask(parseRouter, RawSentece(sentence)).mapTo[ParsedSentence]
		}
		
		val parsed = Await.result(Future.sequence(parseFutures), timeout.duration) 
		
		origin ! NLPResponse(parsed)
	}
}