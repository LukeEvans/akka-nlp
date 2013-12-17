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
import com.winston.nlp.worker.SplitActor
import com.winston.nlp.worker.ParseActor
import akka.routing.RoundRobinRouter
import com.winston.nlp.search.ElasticSearchActor
import com.winston.nlp.scoring.ScoringActor
import com.winston.nlp.worker.PackagingActor
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.ClusterEvent.MemberUp
import com.winston.nlp.listener.Listener
import com.winston.nlp.worker.URLExtractorActor


//class ApiBoot(args: Array[String]) extends Bootable {
class ApiBoot extends Bootable {

	val ip = IPTools.getPrivateIp();
      
	println("IP: " + ip)
	
	val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2551") 
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [reducto-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("reducto"))
  
	
    implicit val system = ActorSystem("NLPClusterSystem-0-1", config)
    
    //#registerOnUp
    Cluster(system) registerOnMemberUp {
	  
	  
	    // Easy role change for debugging
          val role = "reducto-backend"
          val parse_role = "reducto-backend"
          val default_parallelization = 10
          val search_parallelization = 4
          val parse_parallelization = 4

		  // URLExtraction Router
		  val URLExtractorRouter = system.actorOf(Props[URLExtractorActor].withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 1, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
			name = "URLExtractorRouter")
			
		  // Splitting router
		  val splitRouter = system.actorOf(Props(classOf[SplitActor], URLExtractorRouter).withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = default_parallelization,
			allowLocalRoutees = true, useRole = Some(role)))),
			name = "splitRouter")
			  
		  // Parsing router
		  val parseRouter = system.actorOf(Props[ParseActor].withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 1, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some(parse_role)))),
			name = "parseRouter")
		
		  // Search router
		  val elasticSearchRouter = system.actorOf(Props[ElasticSearchActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = search_parallelization,
			allowLocalRoutees = true, useRole = Some(role)))),
			name = "elasticSearchRouter")
			  
		  // Scoring router
		  val scoringRouter = system.actorOf(Props(classOf[ScoringActor], elasticSearchRouter).withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = default_parallelization,
			allowLocalRoutees = true, useRole = Some(role)))),
			name = "scoringRouter")
		
		  // Package router
		  val packageRouter = system.actorOf(Props[PackagingActor].withRouter(ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = default_parallelization,
			allowLocalRoutees = true, useRole = Some(role)))),
			name = "packageRouter")
		  
		  // Reducto Router
		  val reductoRouter = system.actorOf(Props(classOf[ReductoActor],splitRouter, parseRouter, scoringRouter, packageRouter).withRouter(
		   	ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
		   	ClusterRouterSettings(
		   	totalInstances = 100, maxInstancesPerNode = default_parallelization,
		   	allowLocalRoutees = true, useRole = Some(role)))),
		   	name = "reductoActors")
   	
		// Actor actually handling the requests
   		val service = system.actorOf(Props(classOf[ApiActor], reductoRouter).withRouter(
    	  ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
    	  ClusterRouterSettings(
    	  totalInstances = 100, maxInstancesPerNode = 3,
    	  allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
    	  name = "serviceRouter")
    		  
       IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
    }
	
  
    def startup(){
	      val clusterListener = system.actorOf(Props(classOf[Listener], system),
	    		  name = "clusterListener")
	      Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
	}

	def shutdown(){
		system.shutdown()
	}
}

object ApiApp {
   def main(args: Array[String]) = {
     val api = new ApiBoot
     api.startup
   }
}
