package com.winston.nlp.scoring

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

class ScoringMaster extends Master {
	
  log.info("Scoring master starting...")
  
  // Scoring router
  val scoringRouter = context.actorOf(Props(classOf[ScoringWorker], self).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
      ClusterRouterSettings(
	  totalInstances = 100, maxInstancesPerNode = 1,
	  allowLocalRoutees = true, useRole = Some("reducto-backend")))),
	  name = "scoringRouter")
	  
}