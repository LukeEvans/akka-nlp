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
import akka.routing.FromConfig


class ReductoActor(splitRouter:ActorRef, parseRouter:ActorRef, scoringRouter:ActorRef, packageRouter:ActorRef) extends Actor { 
  
    case class ReductoIntermediate(parsed:List[SentenceContainer], scored:SetContainer)
  
    println("\n\n\n\nstarting reducto\n\n\n\n")
	  
	def receive = {
		case RequestContainer(request) =>
		  val origin = sender;
		  process(request, origin);
		case HammerRequestContainer(request) =>
		  val origin = sender;
		  sentencesSize(request, origin)
	}

    def sentencesSize(request: ReductoRequest, origin: ActorRef) {
    	implicit val timeout = Timeout(5 seconds);
    	
		// Split sentences
		val split = (splitRouter ? RequestContainer(request)).mapTo[SetContainer];
		
		split onComplete {
		  case Success(result) => 
		    val set = result.set;
		    
		    // Parse sentences
		    val parseFutures: List[Future[SentenceContainer]] = set.sentences.toList map { sentence =>
		    	(parseRouter ? SentenceContainer(sentence.copy)).mapTo[SentenceContainer]
            }

		    // Sequence list
		    val futureParsed = Future.sequence(parseFutures)
		    
		     // Score the sentences
		    val futureScored = (scoringRouter ? SetContainer(set)).mapTo[SetContainer];
		    
		    val resultFuture =  for {
		      parsed <- futureParsed
		      scored <- futureScored
		    } yield ReductoIntermediate(parsed, scored)
		    
		    resultFuture map { item =>
		      
		      val newSet = item.scored.set;
		      
		      // Replace old sentences with new
		      item.parsed map { sc =>
		        newSet.addTreeToSentence(sc.sentence)
		      }
		      
		       origin ! SetContainer(set)
		    }
		    
		 case Failure(failure) => println(failure)
		}
    }
    
    // Process Request
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

		    // Sequence list
		    val futureParsed = Future.sequence(parseFutures)
		    
		    // Score the sentences
		    val futureScored = (scoringRouter ? SetContainer(set)).mapTo[SetContainer];
		    
		    val resultFuture =  for {
		      parsed <- futureParsed
		      scored <- futureScored
		    } yield ReductoIntermediate(parsed, scored)
		    
		    resultFuture map { item =>
		      
		      val newSet = item.scored.set;
		      
		      // Replace old sentences with new
		      item.parsed map { sc =>
		        newSet.addTreeToSentence(sc.sentence)
		      }

		      val futureResult = (packageRouter ? SetContainer(newSet)).mapTo[ResponseContainer];
		      
		      futureResult map { result =>
			  	origin ! result
			  }			      
		    }
		    
		  case Failure(failure) => println(failure)
		}
    }
}