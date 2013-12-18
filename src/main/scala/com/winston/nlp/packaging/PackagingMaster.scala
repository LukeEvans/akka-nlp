package com.winston.nlp.packaging

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

class PackagingMaster extends Master {
	
  log.info("Packaging master starting...")
  
  // Packaging router
  val packagingRouter = context.actorOf(Props(classOf[PackagingWorker], self).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
      ClusterRouterSettings(
	  totalInstances = 100, maxInstancesPerNode = 1,
	  allowLocalRoutees = true, useRole = Some("reducto-backend")))),
	  name = "packagingRouter")
	  
}