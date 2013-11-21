package com.winston.nlp.worker

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.winston.nlp.transport.messages._
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
import com.winston.nlp.NLPSentence
import com.winston.nlp.SummaryResult
import com.winston.nlp.transport.ReductoRequest
import com.winston.nlp.transport.ReductoRequest


class ReductoActor extends Actor { 
  
    case class ReductoIntermediate()
  
	// Splitting router
    val splitRouter = context.actorOf(Props[SplitActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	    ClusterRouterSettings(
	    totalInstances = 100, maxInstancesPerNode = 1,
	    allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
	  name = "splitRouter")
	  
	// Parsing router
	val parseRouter = context.actorOf(Props[ParseActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.HeapMetricsSelector), 
	    ClusterRouterSettings(
	    totalInstances = 100, maxInstancesPerNode = 1,
	    allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
	  name = "parseRouter")
	  
	// Scoring actor
	val scoringRouter = context.actorOf(Props[ScoringActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	    ClusterRouterSettings(
	    totalInstances = 100, maxInstancesPerNode = 1,
	    allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
	  name = "scoringRouter")

	// Package actor
	val packageRouter = context.actorOf(Props[PackagingActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
	    ClusterRouterSettings(
	    totalInstances = 100, maxInstancesPerNode = 1,
	    allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
	  name = "packageRouter")
	  
	def receive = {
		case RequestContainer(request) =>
		  val origin = sender;
		  process(request, origin);
	}

    // Process Raw Text
    def process(request: ReductoRequest, origin: ActorRef) {
    	implicit val timeout = Timeout(500 seconds);
		import context.dispatcher
		
		// Split sentences
		val split = (splitRouter ? RequestContainer(request)).mapTo[SetContainer];
		
		split onComplete {
		  case Success(result) => 
		    val set = result.set;
		    
		    // Parse sentences
		    val parseFutures: List[Future[SentenceContainer]] = set.sentences.toList map { sentence =>
		    	(parseRouter ? SentenceContainer(sentence.copy)).mapTo[SentenceContainer]
            }
			
		    // Add parsed sentences back into the set at proper location
		    parseFutures map { list =>
		      list map { sc =>
		        set.replaceSentence(sc.sentence)
		      }
		    }
		    
		    // Score the sentences
		    val futureScored = (scoringRouter ? SetContainer(set)).mapTo[SetContainer];
		    
			futureScored map { scored =>
				
			  	val futureResult = (packageRouter ? scored).mapTo[ResponseContainer];
			  	
			  	futureResult map { result =>
			  	  origin ! result
			  	}
		 	}
			
		  case Failure(failure) => println(failure)
		}
    }
}