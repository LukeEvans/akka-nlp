package com.winston.api

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import com.reactor.nlp.utilities.IPTools
import com.typesafe.config.ConfigFactory
import akka.cluster.Cluster
import com.winston.nlp.worker.ReductoActor
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterSettings
import akka.kernel.Bootable


class ApiBoot(args: Array[String]) extends Bootable {

	val ip = IPTools.getPrivateIp();
      
	println("IP: " + ip)
	
    val config = (if (args.nonEmpty) ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}") else ConfigFactory.empty)
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [reducto-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("reducto"))
      
    implicit val system = ActorSystem("NLPClusterSystem-0-1", config)
        
    //#registerOnUp
    Cluster(system) registerOnMemberUp {
       val service = system.actorOf(Props[ApiActor].withRouter(
    	  ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
    	  ClusterRouterSettings(
    	  totalInstances = 100, maxInstancesPerNode = 1,
    	  allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
    	  name = "serviceRouter")
    		 
       IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
    }
  
    def startup(){
	}

	def shutdown(){
		system.shutdown()
	}
}

object ApiApp {
   def main(args: Array[String]) = {
     val api = new ApiBoot(args)
   }
}
