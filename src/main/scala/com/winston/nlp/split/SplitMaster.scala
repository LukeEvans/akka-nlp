package com.winston.nlp.split

import com.winston.nlp.MasterWorker.Master
import akka.actor.Props
import akka.cluster.routing.ClusterRouterConfig
import akka.routing.RoundRobinRouter
import akka.cluster.routing.ClusterRouterSettings

class SplitMaster(parallel:Int, role:String) extends Master("split-master") {

   log.info("Split master starting...")
  
  // Parsing router
  val splitRouter = context.actorOf(Props(classOf[SplitWorker], self).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
      ClusterRouterSettings(
	  totalInstances = 1000, maxInstancesPerNode = parallel,
	  allowLocalRoutees = true, useRole = Some(role)))),
	  name = "splitRouter")
	  
}