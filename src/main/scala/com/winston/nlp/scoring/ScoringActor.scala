package com.winston.nlp.scoring

import akka.actor.Actor
import org.apache.xpath.operations.String
import com.winston.nlp.messages.SetContainer
import com.winston.nlp.nlp.SentenceSet
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.RoundRobinRouter
import com.winston.nlp.messages.SetContainer
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import com.winston.nlp.messages.TermFrequencyResponse
import com.winston.nlp.search.ElasticSearchActor

class ScoringActor extends Actor {

	val elasticSearchRouter = context.actorOf(Props(classOf[ElasticSearchActor]).withRouter(RoundRobinRouter(nrOfInstances = 3)));
	println("ES Router created");

	val termFrequencyRouter = context.actorOf(Props(classOf[TermFrequencyActor],elasticSearchRouter).withRouter(RoundRobinRouter(nrOfInstances = 1)));
	println("TF Router created");
	
	def receive = {
		case set: SetContainer => processScore(set.set, sender);
	}

	def processScore(set:SentenceSet, origin:ActorRef) {
		implicit val timeout = Timeout(500 seconds);
		
		// Determine the number of total documents
		
		// Find stop phrases
		
		// Find term frequencies
		val response = termFrequencyRouter ? SetContainer(set)
		val TermFrequencyResponse(frequencyMap) = Await.result(response, timeout.duration).asInstanceOf[TermFrequencyResponse]
		
		println(frequencyMap)
	}
}