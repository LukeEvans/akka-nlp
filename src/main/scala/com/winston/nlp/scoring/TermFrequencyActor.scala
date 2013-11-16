package com.winston.nlp.scoring

import com.winston.nlp.messages.SetContainer
import com.winston.nlp.SentenceSet
import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.JavaConversions._
import com.winston.nlp.messages.TermFrequencyResponse
import com.winston.nlp.messages.SentenceContainer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import com.winston.nlp.messages.SingleTermFrequency
import java.util.ArrayList
import scala.concurrent.Await

class TermFrequencyActor(searchRouter:ActorRef) extends Actor {

  	def receive = {
		case sc: SetContainer =>
		  val origin = sender;
		  processTermFrequency(sc.set, origin);
	}
  	
  	def processTermFrequency(set:SentenceSet, origin:ActorRef) {
  	  implicit val timeout = Timeout(5 seconds);
  		
  	  // Build list of words that should be looked for
  	  val wordList = new ArrayList[String]
  	  
  	  set.sentences.toList map { sentence =>
  	    sentence.words.toList map { word =>
  	      if (!wordList.contains(word.value)) {
  	        wordList += word.value
  	      }
  	    }
  	  }
  	  
  	  // Get the parsed sentences
	  val frequencyFutures: List[Future[SingleTermFrequency]] = wordList.toList map { term =>
	  	ask(searchRouter, SingleTermFrequency(term, 0)).mapTo[SingleTermFrequency]
  	  } 
  	  
  	  
	  val wordMap = scala.collection.mutable.Map[String, Long]();
	  
	  val frequencyList = Await.result(Future.sequence(frequencyFutures), timeout.duration) 
	  
	  frequencyList map { termFreq =>
	    wordMap += (termFreq.word -> termFreq.count);
	  }
	  
  	  origin ! TermFrequencyResponse(wordMap.toMap)
  	}
}