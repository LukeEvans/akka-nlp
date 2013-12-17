package com.winston.nlp.scoring

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import org.apache.xpath.operations.String
import com.winston.nlp.SentenceSet
import com.winston.nlp.transport.messages._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Scheduler
import akka.pattern.{ ask, pipe, CircuitBreaker, CircuitBreakerOpenException }
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import com.winston.nlp.transport.ErrorResponse
//import play.api.libs.concurrent.Execution.Implicits._

class ScoringActor(searchRouter:ActorRef) extends Actor with ActorLogging{
	import context.dispatcher
	// Case class for future compositions
    case class ScoringIntermediateObject(totalDocs:LongContainer, stopPhrases:StopPhrasesObject, frequencies:TermFrequencyResponse);
  
	val termFrequencyRouter = context.actorOf(Props(classOf[TermFrequencyActor],searchRouter).withRouter(RoundRobinRouter(nrOfInstances = 1)));
	
	val breaker =
	  new CircuitBreaker(context.system.scheduler,
	      maxFailures = 5,
	      callTimeout = (2).seconds,
	      resetTimeout = 1.minute).onOpen(notifyMeOnOpen())
	      
	      
    def notifyMeOnOpen(): Unit = {
    	log.warning("CircuitBreaker is now open, and will not close for one minute")
	}
	
	def receive = {
		case set: SetContainer =>
		  val origin = sender;
		  processScore(set.set, origin);
	}

	def processScore(set:SentenceSet, origin:ActorRef) {
		implicit val timeout = Timeout(500 seconds);
		import context.dispatcher
		println("scoring future call")

		try{
			val futureTD = (searchRouter ? breaker.withSyncCircuitBreaker(LongContainer(0))).mapTo[LongContainer]
			val futureSP = (searchRouter ? breaker.withSyncCircuitBreaker(StopPhrasesObject())).mapTo[StopPhrasesObject]
			val futureFQ = (termFrequencyRouter ? breaker.withSyncCircuitBreaker(SetContainer(set, 0))).mapTo[TermFrequencyResponse]
			
			val future = for {
			 totalDocs <- futureTD
			 stopPhrases <- futureSP
			 frequencies <- futureFQ
			} yield ScoringIntermediateObject(totalDocs, stopPhrases, frequencies)
			
			println("scoring futures composed")
			
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
				
				origin ! SetContainer(set, 0)
			}
		} catch{
		  	case e:CircuitBreakerOpenException =>{
		  		e.printStackTrace()
		  		origin ! CircuitBreakException(new ErrorResponse("ElasticSearch Circuit Break"))
		  	}
		  	case e:Exception => {
		  		e.printStackTrace()
		  		origin ! SetContainer(null, 0)
		  	}
		}
	}
}