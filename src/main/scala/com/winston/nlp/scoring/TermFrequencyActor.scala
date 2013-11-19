package com.winston.nlp.scoring

import com.winston.nlp.messages.SetContainer
import com.winston.nlp.SentenceSet
import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.JavaConversions._
import com.winston.nlp.messages.TermFrequencyResponse
import com.winston.nlp.messages.SentenceContainer
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import com.winston.nlp.messages.SingleTermFrequency
import java.util.ArrayList
import scala.concurrent.Await
import akka.actor.Status.Failure
import akka.actor.Status.Success
import com.winston.nlp.messages.SingleTermFrequency
import scala.concurrent.Future
import com.winston.nlp.messages.TermFrequencyResponse

class TermFrequencyActor(searchRouter:ActorRef) extends Actor {

  	def receive = {
		case sc: SetContainer =>
		  val origin = sender;
		  processTermFrequency(sc.set, origin);
	}
  	
  	def processTermFrequency(set:SentenceSet, origin:ActorRef) {
  	  implicit val timeout = Timeout(50 seconds);
  	  import context.dispatcher
  		
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

  	  // Collect
  	  Future sequence(frequencyFutures) map { list =>
  	  	list map { termFreq =>
  	  		wordMap += (termFreq.word -> termFreq.count);
  	  	}
  	  	origin ! TermFrequencyResponse(wordMap.toMap)
  	  }

  	}
}