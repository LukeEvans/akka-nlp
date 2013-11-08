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
import com.winston.nlp.scoring.ScoringActor
import akka.routing.RoundRobinRouter


class NLPActor(splitRouter:ActorRef, parseRouter:ActorRef) extends Actor { 
	// Scoring router
	val scoringRouter = context.actorOf(Props[ScoringActor].withRouter(RoundRobinRouter(nrOfInstances = 1)));
	println("Scoring Router created");
	
	def receive = {
		case raw_text: RawText => process(raw_text, sender);
	}

	// Process Raw Text
	def process(rawText: RawText, origin: ActorRef) {
		implicit val timeout = Timeout(5 seconds);

		// Get the split sentences
		val futureSplit = splitRouter ? rawText; 
		val splitSet = Await.result(futureSplit, timeout.duration).asInstanceOf[SetContainer];

		var set = splitSet.set;
		
		// Get the parsed sentences
		val parseFutures: List[Future[SentenceContainer]] = set.sentences.toList map { sentence =>
			ask(parseRouter, SentenceContainer(sentence)).mapTo[SentenceContainer]
		}
		
		val parsed = Await.result(Future.sequence(parseFutures), timeout.duration) 
		
		parsed map { sc =>
			set.replaceSentence(sc.sentence);
		}
		
		// Score sentences
		scoringRouter ! SetContainer(set);
		
		origin ! SetContainer(set)
	}
}