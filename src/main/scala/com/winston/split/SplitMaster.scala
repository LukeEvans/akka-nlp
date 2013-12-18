package com.winston.split

import com.winston.nlp.MasterWorker.Master
import akka.actor.Props
import akka.cluster.routing.ClusterRouterConfig
import akka.routing.RoundRobinRouter
import akka.cluster.routing.ClusterRouterSettings

class SplitMaster extends Master {

   log.info("Split master starting...")
  
  // Parsing router
  val splitRouter = context.actorOf(Props(classOf[SplitWorker], self).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
      ClusterRouterSettings(
	  totalInstances = 100, maxInstancesPerNode = 1,
	  allowLocalRoutees = true, useRole = Some("reducto-backend")))),
	  name = "splitRouter")
	  
}