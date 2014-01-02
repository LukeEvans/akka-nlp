package com.winston.nlp.split

import akka.actor.ActorRef
import com.winston.nlp.MasterWorker.Worker
import akka.actor.Props
import com.winston.split.SplitActor

class SplitWorker(master: ActorRef) extends Worker(master) {
  // We'll use the current dispatcher for the execution context.
  implicit val ec = context.dispatcher
 
  log.info("Split Worker staring")
  
  // Start parse actor
  val splitActor = context.actorOf(Props(classOf[SplitActor], self), "splitter")
  
  // Handle work
  def doWork(workSender: ActorRef, msg: Any): Unit = {
      splitActor.tell(msg, workSender)
  }
}