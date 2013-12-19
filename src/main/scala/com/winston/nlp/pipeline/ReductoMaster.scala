package com.winston.nlp.pipeline

import akka.actor.Terminated
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.cluster.routing.ClusterRouterConfig
import akka.routing.RoundRobinRouter
import akka.cluster.routing.ClusterRouterSettings
import scala.collection.mutable.{Map, Queue}
import com.winston.nlp.MasterWorker.MasterWorkerProtocol._
import com.winston.nlp.MasterWorker.Master

class ReductoMaster(parallel:Int, role:String, split:ActorRef, parse:ActorRef, score:ActorRef, pack:ActorRef) extends Master {
	
  log.info("Reducto pipeline master starting...")
  
  // Reducto router
  val reductoRouter = context.actorOf(Props(classOf[ReductoWorker], self, split, parse, score, pack).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
      ClusterRouterSettings(
	  totalInstances = 100, maxInstancesPerNode = parallel,
	  allowLocalRoutees = true, useRole = Some(role)))),
	  name = "reductoRouter")
	  
}