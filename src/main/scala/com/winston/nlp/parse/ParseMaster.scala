package com.winston.nlp.parse

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

class ParseMaster(parallel:Int, role:String) extends Master {
	
  log.info("Parse master starting...")
  
  // Parsing router
  val parseRouter = context.actorOf(Props(classOf[ParseWorker], self).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
      ClusterRouterSettings(
	  totalInstances = 100, maxInstancesPerNode = parallel,
	  allowLocalRoutees = true, useRole = Some(role)))),
	  name = "parseRouter")
	  
}