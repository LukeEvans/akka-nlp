package com.winston.nlp.scoring

import com.winston.nlp.MasterWorker.Worker
import akka.actor.ActorPath
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.{pipe, ask}
import akka.actor.Props
import akka.util.Timeout
import scala.concurrent.duration._
import com.winston.nlp.search.ElasticSearchActor

class ScoringWorker(master: ActorRef) extends Worker(master) {
  // We'll use the current dispatcher for the execution context.
  implicit val ec = context.dispatcher
 
  log.info("Scoring Worker staring")
  
  // Start scoring actor
  val searchActor = context.actorOf(Props[ElasticSearchActor], "search")
  val scoringActor = context.actorOf(Props(classOf[ScoringActor], self, searchActor), "scorer")
  
  // Handle work
  def doWork(workSender: ActorRef, msg: Any): Unit = {
      scoringActor.tell(msg, workSender)
  }
    
}