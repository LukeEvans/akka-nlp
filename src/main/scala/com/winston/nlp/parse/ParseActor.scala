package com.winston.nlp.parse

import com.winston.nlp.transport.messages._
import scala.concurrent.duration._
import akka.actor.Actor
import scala.compat.Platform
import scala.util.Random
import com.winston.nlp.NLPSentence
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

class ParseActor extends Actor {

	val parser = new NLPParser()
	val name = Random.nextInt
	var longest = 0.0;
	
	override def preStart() {
	  println("--Creating Parser");
      self ! InitRequest
	}
	
	override def postStop() {
		println("--Stopped parser");
	}
	
	def receive = {
	  	case InitRequest => 
	  	  parser.init(); 
		case sc:SentenceContainer =>
		  val origin = sender;
		  processWithTimeout(sc.sentence, origin)
	}
	
	//================================================================================
	// Process with timeout
	//================================================================================
	def processWithTimeout(sentence:NLPSentence, origin:ActorRef) {	
	  import context.dispatcher
	  import akka.pattern.after
	  
	  val start = Platform.currentTime
	  val sc = parser.parseProcess(sentence)
	  val durr = Platform.currentTime - start
	  
	  if (durr > longest) {
	    longest = durr;
	    println(name + "- " + sentence.index + " " + durr)
	  }
	  
	  origin ! sc
	}
}