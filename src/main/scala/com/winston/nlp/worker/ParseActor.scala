package com.winston.nlp.worker

import com.winston.nlp.annotate.NLPParser
import com.winston.nlp.transport.messages._
import akka.actor.Actor
import scala.compat.Platform
import scala.util.Random

class ParseActor extends Actor {

	val parser = new NLPParser()
	val name = Random.nextInt
	
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
		  val start = Platform.currentTime
		  
		  val returnSentenceContainer = parser.parseProcess(sc.sentence);
		  val duration = Platform.currentTime - start;
		  returnSentenceContainer.sentence.parseInfo = "\n" + name + " " + sc.sentence.index +": " + duration + " ms"
		  println("\n" + name + " " + sc.sentence.index +": " + duration + " ms")
		  
		  sender ! returnSentenceContainer
	}
}