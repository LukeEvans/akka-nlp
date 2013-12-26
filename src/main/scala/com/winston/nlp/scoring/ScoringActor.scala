package com.winston.nlp.scoring

import akka.actor.Actor
import org.apache.xpath.operations.String
import com.winston.nlp.SentenceSet
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.RoundRobinRouter
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import com.winston.nlp.search.ElasticSearchActor
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterSettings
import com.winston.nlp.transport.messages._
import com.winston.nlp.MasterWorker.MasterWorkerProtocol._

class ScoringActor(manager:ActorRef, searchRouter:ActorRef) extends Actor {

	// Case class for future compositions
    case class ScoringIntermediateObject(totalDocs:LongContainer, stopPhrases:StopPhrasesObject, frequencies:TermFrequencyResponse);
  
	val termFrequencyRouter = context.actorOf(Props(classOf[TermFrequencyActor],searchRouter).withRouter(RoundRobinRouter(nrOfInstances = 1)));
	
	manager ! ReadyForWork
	
	def receive = {
		case set: SetContainer =>
		  val origin = sender;
		  processScore(set.set, origin);
	}

	def processScore(set:SentenceSet, origin:ActorRef) {
		implicit val timeout = Timeout(500 seconds);
		import context.dispatcher
		
		val futureTD = (searchRouter ? LongContainer(0)).mapTo[LongContainer]
		val futureSP = (searchRouter ? StopPhrasesObject()).mapTo[StopPhrasesObject]
		val futureFQ = (termFrequencyRouter ? SetContainer(set)).mapTo[TermFrequencyResponse]
		
		val future = for {
		 totalDocs <- futureTD
		 stopPhrases <- futureSP
		 frequencies <- futureFQ
		} yield ScoringIntermediateObject(totalDocs, stopPhrases, frequencies)
		
		future map { item =>
		  
		  	// Calculate cosine score
		  	set.calculateCosinSim;
		  	
			// Set total docs
			set.putTotalCount(item.totalDocs.long)
			
			// Add word frequencies
			set.addWordFrequencies(item.frequencies.mapObject)
			
			// Mark invalid words
			set.markInavlidWords(item.stopPhrases.phrases);
			
			// Find index counts
			set.findTotalObservedCounts;
	
			// Find total terms
			set.findTotalTermsInDoc;
			
			// Calculate TFIDF
			set.calculateTFIDF;
			
			// Calculate weight
			set.calculateWeight;
			
			origin.tell(SetContainer(set), manager)
			manager ! WorkComplete("Done")
		}
	}
}