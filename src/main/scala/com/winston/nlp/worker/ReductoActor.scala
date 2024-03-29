package com.winston.nlp.worker

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.winston.nlp.messages._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.winston.nlp.SentenceSet
import scala.collection.JavaConversions._
import com.winston.nlp.scoring.ScoringActor
import akka.routing.RoundRobinRouter
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterSettings
import com.winston.nlp.scoring.ScoringActor
import akka.routing.Broadcast
import com.winston.nlp.combinations.SentenceCombinations


class ReductoActor extends Actor { 
  
	// Splitting router
    val splitRouter = context.actorOf(Props[SplitActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	    ClusterRouterSettings(
	    totalInstances = 100, maxInstancesPerNode = 1,
	    allowLocalRoutees = true, useRole = Some("nlp-frontend")))),
	  name = "splitRouter")
	  
	// Parsing router
	val parseRouter = context.actorOf(Props[ParseActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.HeapMetricsSelector), 
	    ClusterRouterSettings(
	    totalInstances = 100, maxInstancesPerNode = 1,
	    allowLocalRoutees = true, useRole = Some("nlp-frontend")))),
	  name = "parseRouter")
	  
	// Scoring actor
	val scoringRouter = context.actorOf(Props[ScoringActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	    ClusterRouterSettings(
	    totalInstances = 100, maxInstancesPerNode = 1,
	    allowLocalRoutees = true, useRole = Some("nlp-frontend")))),
	  name = "scoringRouter")

	def receive = {
		case raw_text: RawText =>
		  val origin = sender;
		  process(raw_text, origin);
	}

	// Process Raw Text
	def process(rawText: RawText, origin: ActorRef) {
		implicit val timeout = Timeout(500 seconds);

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
		val futureSet = scoringRouter ? SetContainer(set);
		val SetContainer(scoredSet) = Await.result(futureSet, timeout.duration).asInstanceOf[SetContainer];
		
		// Find Highest combo
		val combos = new SentenceCombinations(set.sentences)
		val c = combos.getHighestCombo(3, true);
		
		origin ! SetContainer(scoredSet)
	}
}