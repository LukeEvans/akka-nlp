package com.winston.nlp.pipeline

import com.winston.nlp.MasterWorker.Worker
import akka.actor.ActorPath
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.{pipe, ask}
import akka.actor.Props
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._

class ReductoWorker(master: ActorRef, split:ActorRef, parse:ActorRef, score:ActorRef, pack:ActorRef, url:ActorRef) extends Worker(master) {
  // We'll use the current dispatcher for the execution context.
  implicit val ec = context.dispatcher
 
  log.info("Reducto Worker staring")
  
  // Start reducto actor
  val reductoActor = context.actorOf(Props(classOf[ReductoActor], self, split, parse, score, pack, url), "reducto")

  // Handle work
  def doWork(workSender: ActorRef, msg: Any): Unit = {
      reductoActor.tell(msg, workSender)
  }
    
}