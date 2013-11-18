package com.winston.nlp.scoring

import akka.actor.Actor
import org.apache.xpath.operations.String
import com.winston.nlp.messages.SetContainer
import com.winston.nlp.SentenceSet
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
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterSettings
import com.winston.nlp.messages._

class ScoringActor extends Actor {

  	// Search router
    val elasticSearchRouter = context.actorOf(Props[ElasticSearchActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	    ClusterRouterSettings(
	    totalInstances = 100, maxInstancesPerNode = 10,
	    allowLocalRoutees = true, useRole = Some("nlp-frontend")))),
	  name = "elasticSearchRouter")
	  
	val termFrequencyRouter = context.actorOf(Props(classOf[TermFrequencyActor],elasticSearchRouter).withRouter(RoundRobinRouter(nrOfInstances = 1)));
	
	def receive = {
		case set: SetContainer =>
		  val origin = sender;
		  processScore(set.set, origin);
	}

	def processScore(set:SentenceSet, origin:ActorRef) {
		implicit val timeout = Timeout(500 seconds);
		
		///////////////////////////
		// Send out to be processed
		///////////////////////////
		
		// Determine the number of total documents
		val futureTD = elasticSearchRouter ? LongContainer(0);
		
		// Find stop phrases
		val futureSP = elasticSearchRouter ? StopPhrasesObject();
		
		// Find term frequencies
		val futureFrequencies = termFrequencyRouter ? SetContainer(set)
		
		///////////////////////////
		// Intermediate Process
		///////////////////////////
		
		// Calculate Cosine
		set.calculateCosinSim;
		
		///////////////////////////
		// Collect
		///////////////////////////
		
		// Collect total docs
		val TD = Await.result(futureTD, timeout.duration).asInstanceOf[LongContainer]
		
		// Collect stop phrases
		val StopPhrasesObject(stopPhrases) = Await.result(futureSP, timeout.duration).asInstanceOf[StopPhrasesObject];
		
		// Collect frequencies
		val TermFrequencyResponse(frequencyMap) = Await.result(futureFrequencies, timeout.duration).asInstanceOf[TermFrequencyResponse]
		
		///////////////////////////
		// Post processing
		///////////////////////////
		
		// Set total docs
		set.putTotalCount(TD.long)
		
		// Add word frequencies
		set.addWordFrequencies(frequencyMap)
		
		// Mark invalid words
		set.markInavlidWords(stopPhrases);
		
		// Find index counts
		set.findTotalObservedCounts;

		// Find total terms
		set.findTotalTermsInDoc;
		
		// Calculate TFIDF
		set.calculateTFIDF;
		
		// Calculate weight
		set.calculateWeight;
		
		origin ! SetContainer(set)
	}
}