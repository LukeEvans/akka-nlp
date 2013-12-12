package com.winston.nlp.worker

import com.winston.nlp.annotate.NLPParser
import com.winston.nlp.transport.messages._
import scala.concurrent.duration._
import akka.actor.Actor
import scala.compat.Platform
import scala.util.Random
import com.winston.nlp.NLPSentence
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.util.Timeout
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Await
import scala.util.Success
import scala.util.Failure
import scala.util.control.NonFatal
import akka.actor.Scheduler
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise
import scala.util.Success
import scala.util.Failure
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.ClusterEvent.MemberRemoved
import akka.actor.ActorLogging
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.Cluster
import akka.actor.Props

class ParseActor extends Actor {

	val parser = new NLPParser()
	val name = Random.nextInt
	var longest = 0.0;
	//val clusterListener = context.system.actorOf(Props[ClusterListener], name = "clusterListener")
	
	override def preStart() {
	  println("--Creating Parser");
	  //Cluster(context.system).subscribe(clusterListener, classOf[ClusterDomainEvent])
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