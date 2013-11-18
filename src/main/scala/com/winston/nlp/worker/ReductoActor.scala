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
import scala.util.Success
import scala.util.Failure


class ReductoActor extends Actor { 
  
    case class ReductoIntermediate()
  
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
		import context.dispatcher
		
		val split = (splitRouter ? rawText).mapTo[SetContainer];
		
		split onComplete {
		  case Success(result) => 
		       val set = result.set;
		       
		  	   var parseFutures = List[Future[SentenceContainer]]();
			   result.set.sentences map { sentence =>
			   	  parseFutures.+: ((parseRouter ? SentenceContainer(sentence)).mapTo[SentenceContainer])
			   }
			   
			   Future sequence(parseFutures) map { list =>
			     list map { sc =>
			       set.replaceSentence(sc.sentence);
			     }
			   }
			   
			   val futureScored = (scoringRouter ? SetContainer(set)).mapTo[SetContainer];
			   
			   futureScored map { scored =>
			     val combos = new SentenceCombinations(scored.set.sentences);
			     val c = combos.getHighestCombo(3, true);
			     
			     origin ! scored;
			   }
			   
			   
		  case Failure(failure) => println("Failure")
		}
	}
	
}