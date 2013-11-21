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


object ApiApp{
  def main(args: Array[String]) ={

        val ip = IPTools.getPrivateIp();
        
        val config = (if (args.nonEmpty) ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}") else ConfigFactory.empty)
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [reducto-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("reducto"))
      
        implicit val system = ActorSystem("NLPClusterSystem-0-1", config)
        
         //#registerOnUp
        Cluster(system) registerOnMemberUp {
          val service = system.actorOf(Props[ApiActor2].withRouter(
        		  ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
        		  ClusterRouterSettings(
        		  totalInstances = 100, maxInstancesPerNode = 1,
        		  allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
        		  name = "serviceRouter")
    		 
           IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
        }
  }
}