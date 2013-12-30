package com.winston.nlp.pipeline

import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import com.winston.nlp.transport.messages._
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._
import scala.util.Success
import scala.util.Failure
import com.winston.nlp.transport.ReductoRequest
import com.winston.nlp.MasterWorker.MasterWorkerProtocol._


class ReductoActor(manager:ActorRef, splitMaster:ActorRef, parseMaster:ActorRef, scoringMaster:ActorRef, packagingMaster:ActorRef, urlExtractor:ActorRef) extends Actor { 
  
    case class ReductoIntermediate(parsed:List[SentenceContainer], scored:SetContainer)
  
    println("\n\n\n\nStarting Reducto\n\n\n\n")
	  
    manager ! ReadyForWork
    
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
		val split = (splitMaster ? RequestContainer(request)).mapTo[SetContainer];
		
		split onComplete {
		  case Success(result) => 
		    val set = result.set;
		    
		    // Parse sentences
		    val parseFutures: List[Future[SentenceContainer]] = set.sentences.toList map { sentence =>
		    	(parseMaster ? SentenceContainer(sentence.copy)).mapTo[SentenceContainer]
            }

		    // Sequence list
		    val futureParsed = Future.sequence(parseFutures)
		    
		     // Score the sentences
		    val futureScored = (scoringMaster ? SetContainer(set)).mapTo[SetContainer];
		    
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
		      
		      origin.tell(SetContainer(newSet), manager)
		      manager ! WorkComplete("Done")
		    }
		    
		 case Failure(failure) => println(failure)
		}
    }
    
    // Process Request
    def process(request: ReductoRequest, origin: ActorRef) {
    	implicit val timeout = Timeout(5 second);
    	
    	if(request.headline == null && request.text == null){
    	  var response = for{
    	    extraction <-(urlExtractor ? URLContainer(request.url)).mapTo[URLTextResponse]
    	  }yield extraction
    	  response map{
    	    extractionContainer =>
    	    	request.headline = extractionContainer.extractionTuple._1
    	    	request.text = extractionContainer.extractionTuple._2
    	    	summarize(request, origin)
    	  }
		}
    	else{
    		summarize(request, origin)
    	}
    }
    
    def summarize(request: ReductoRequest, origin:ActorRef){
        implicit val timeout = Timeout(5 second);
      
		// Split sentences
		val split = (splitMaster ? RequestContainer(request)).mapTo[SetContainer];
		
		split onComplete {
		  case Success(result) =>
		    val set = result.set;

		    // Parse sentences
		    val parseFutures: List[Future[SentenceContainer]] = set.sentences.toList map { sentence =>
		    	(parseMaster ? SentenceContainer(sentence.copy)).mapTo[SentenceContainer]
            }

		    // Sequence list
		    val futureParsed = Future.sequence(parseFutures)
		    
		    // Score the sentences
		    val futureScored = (scoringMaster ? ScoringContainer(set, request.decay)).mapTo[SetContainer];
		    
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
		      var numSentences = 3
		      if(request.ratio > 0){
		        var totalSentences = newSet.sentences.size()
		        var ratioSentences = (totalSentences * request.ratio).asInstanceOf[Int]
		        numSentences = if(ratioSentences>0) ratioSentences else 1 
		      }
		      else if (request.sentences != null && request.sentences > 0)
		        numSentences = request.sentences
		        
		      val futureResult = (packagingMaster ? PackagingContainer(newSet, numSentences, request.separationRules)).mapTo[ResponseContainer];
		      
		      futureResult map { result =>
		        origin.tell(result, manager)
		        manager ! WorkComplete("Done")
			  }			      
		    }
		    
		  case Failure(failure) => println(failure)
		}
    }
}