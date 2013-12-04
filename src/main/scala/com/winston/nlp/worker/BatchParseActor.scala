package com.winston.nlp.worker

import java.util.ArrayList
import scala.collection.JavaConversions._
import scala.concurrent.Future
import com.winston.nlp.NLPSentence
import com.winston.nlp.transport.messages.BatchSentenceContainer
import com.winston.nlp.transport.messages.SentenceContainer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.winston.nlp.transport.messages.BatchSentenceContainer
import com.winston.nlp.transport.messages.SentenceContainer
import com.winston.nlp.transport.messages.BatchSentenceContainerResponse

class BatchParseActor(parseRouter:ActorRef) extends Actor {
  	def receive = {
		case BatchSentenceContainer(list, size) =>
		  val origin = sender;
		  processBulk(list, size, origin);
	}
  	
	//================================================================================
	// Process bulk
	//================================================================================
    def processBulk(list:ArrayList[NLPSentence], batchSize:Int,origin:ActorRef) {
  		val listSize = list.size()
  		
  		val listContainer = new ArrayList[ArrayList[NLPSentence]]();
  		
  		// If we have less than (or equal) the batch size, just add what we have and go
  		if (listSize <= batchSize || batchSize <= 0) {
  		  listContainer.add(list)
  		  processLists(listContainer, origin)
  		  return
  		}
  		
  		var taken = 0;
  		var thisList = new ArrayList[NLPSentence]()
  		
  		for (sentence <- list) {
  		  if (taken >= batchSize) {
  			  listContainer.add(thisList)
  			  println("added")
  			  taken = 0;
  			  thisList = new ArrayList[NLPSentence]()
  		  }
  		  
  		  thisList.add(sentence.copy)
  		  println("added sentence: " + sentence)
  		  taken += 1
  		}

  		// Add any leftovers
  		if (thisList.size() > 0) {
  		  listContainer.add(thisList)
  		}
  		
  		// Send off lists
  		processLists(listContainer, origin);
    }

	//================================================================================
	// Process list of lists of sentences
	//================================================================================
    def processLists(listContainer:ArrayList[ArrayList[NLPSentence]], origin:ActorRef) {
    	implicit val timeout = Timeout(5 seconds);
    	
        // Parse sentences
		val parseFutures: List[Future[ArrayList[NLPSentence]]] = listContainer.toList map { list =>
		  println("sending off!")
			(parseRouter ? BatchSentenceContainer(list, -1)).mapTo[ArrayList[NLPSentence]]
        }
		
		val parsedList = new ArrayList[SentenceContainer]()
		
		// Sequence list
		val futureParsed = Future.sequence(parseFutures)		
		
		futureParsed map { res =>
		  res map { list =>
		    list.toList map { sentence =>
		      parsedList.add(SentenceContainer(sentence))
		    }
		  }
		  
		  origin ! BatchSentenceContainerResponse(parsedList.toList)
		}
    }
}