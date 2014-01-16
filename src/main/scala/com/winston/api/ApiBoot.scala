package com.winston.api

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import com.reactor.nlp.utilities.IPTools
import com.typesafe.config.ConfigFactory
import akka.cluster.Cluster
import com.winston.nlp.pipeline.ReductoActor
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterSettings
import akka.kernel.Bootable
import com.winston.split.SplitActor
import com.winston.nlp.parse.ParseActor
import akka.routing.RoundRobinRouter
import com.winston.nlp.search.ElasticSearchActor
import com.winston.nlp.scoring.ScoringActor
import com.winston.nlp.packaging.PackagingActor
import com.winston.nlp.parse.ParseMaster
import com.winston.nlp.split.SplitMaster
import com.winston.nlp.scoring.ScoringMaster
import com.winston.nlp.packaging.PackagingMaster
import com.winston.nlp.pipeline.ReductoMaster
import com.winston.urlextraction.URLExtractorActor
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.winston.nlp.listener.Listener


//class ApiBoot(args: Array[String]) extends Bootable {
class ApiBoot extends Bootable {

	val ip = IPTools.getPrivateIp();
      
	println("IP: " + ip)
	
	val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2552") 
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [reducto-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("reducto"))
      
    implicit val system = ActorSystem("NLPClusterSystem-0-1", config)
        
    //#registerOnUp
    Cluster(system) registerOnMemberUp {
	  
	      // Easy role change for debugging
          val worker_role = "reducto-frontend"
          val parser_role = "reducto-frontend"
          val supervisor_role = "reducto-frontend"
          val default_parallelization = 1
          val score_parallelization = 1
          val parse_parallelization = 1
		    
		  // Splitting master
		  val splitMaster = system.actorOf(Props(classOf[SplitMaster], default_parallelization, worker_role).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some(supervisor_role)))),
			name = "splitMaster")

		  // Parsing master
		  val parseMaster = system.actorOf(Props(classOf[ParseMaster], parse_parallelization, parser_role).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some(supervisor_role)))),
			name = "parseMaster")
			
		  // Scoring master
		  val scoringMaster = system.actorOf(Props(classOf[ScoringMaster], score_parallelization, worker_role).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some(supervisor_role)))),
			name = "scoringMaster")
	
		  // Packaging master
		  val packagingMaster = system.actorOf(Props(classOf[PackagingMaster], default_parallelization, worker_role).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some(supervisor_role)))),
			name = "packagingMaster")
		  
		  // url extractor 
		  val urlExtractorRouter = system.actorOf(Props(classOf[URLExtractorActor]).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some(supervisor_role)))),
			name = "urlExtractorActor")
		  
		  // Recuto master
		  val reductoMaster = system.actorOf(Props(classOf[ReductoMaster], default_parallelization, worker_role, splitMaster, parseMaster, scoringMaster, packagingMaster, urlExtractorRouter).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
			ClusterRouterSettings(
			totalInstances = 100, maxInstancesPerNode = 1,
			allowLocalRoutees = true, useRole = Some(supervisor_role)))),
			name = "reductoMaster")
			
		// Actor actually handling the requests
   		val service = system.actorOf(Props(classOf[ApiActor], reductoMaster).withRouter(	
    	  ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
    	  ClusterRouterSettings(
    	  totalInstances = 100, maxInstancesPerNode = 1,
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
