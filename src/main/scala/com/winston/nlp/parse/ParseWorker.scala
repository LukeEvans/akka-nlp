package com.winston.nlp.parse

import com.winston.nlp.MasterWorker.Worker
import akka.actor.ActorPath
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.pattern.{pipe, ask}
import akka.actor.Props
import akka.util.Timeout
import scala.concurrent.duration._

class ParseWorker(master: ActorRef) extends Worker(master) {
  // We'll use the current dispatcher for the execution context.
  implicit val ec = context.dispatcher
 
  log.info("Parse Worker staring")
  
  // Start parse actor
  val parseActor = context.actorOf(Props(classOf[ParseActor], self), "parser")
  
  // Handle work
  def doWork(workSender: ActorRef, msg: Any): Unit = {
      parseActor.tell(msg, workSender)
//    implicit val timeout = Timeout(5 seconds);
//    
//    val futureParse = parseActor ? msg
//    
//    futureParse pipeTo workSender
//    
//    futureParse map { parse =>
//      self ! WorkComplete(parse)
////      workSender ! parse
//    }
  }
    
}