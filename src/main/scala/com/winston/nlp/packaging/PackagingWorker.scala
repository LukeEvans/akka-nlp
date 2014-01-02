package com.winston.nlp.packaging

import com.winston.nlp.MasterWorker.Worker
import akka.actor.ActorPath
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.{pipe, ask}
import akka.actor.Props
import akka.util.Timeout
import scala.concurrent.duration._

class PackagingWorker(master: ActorRef) extends Worker(master) {
  // We'll use the current dispatcher for the execution context.
  implicit val ec = context.dispatcher
 
  log.info("Packaging Worker staring")
  
  // Start parse actor
  val packagingActor = context.actorOf(Props(classOf[PackagingActor], self), "packager")
  
  // Handle work
  def doWork(workSender: ActorRef, msg: Any): Unit = {
      packagingActor.tell(msg, workSender)
  }
    
}