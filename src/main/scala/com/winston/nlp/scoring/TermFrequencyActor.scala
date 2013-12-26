package com.winston.nlp.scoring

import com.winston.nlp.SentenceSet
import akka.actor.Actor
import akka.actor.ActorRef
import scala.collection.JavaConversions._
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.{ ask, pipe }
import java.util.ArrayList
import scala.concurrent.Await
import akka.actor.Status.Failure
import akka.actor.Status.Success
import scala.concurrent.Future
import com.winston.nlp.transport.messages._

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
  	  
  	  // Send to ElasticSearch Bulk 
  	  val frequencyFuture = (searchRouter ? TermFrequencyBulkReq(wordList.toList)).mapTo[TermFrequencyResponse]

  	  // Pipe result
  	  frequencyFuture pipeTo origin
  	  
  	}
}