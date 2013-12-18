package com.winston.nlp.app

import akka.kernel.Bootable
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import com.winston.nlp.annotate._
import scala.collection.JavaConversions._
import com.reactor.nlp.utilities.IPTools
import com.winston.nlp.worker.SplitActor
import akka.routing.FromConfig
import com.winston.nlp.transport.messages._
import com.winston.nlp.parse.ParseActor
import akka.actor.Inbox
import scala.concurrent.duration._
import com.winston.utlities.Tools
import akka.serialization.SerializationExtension
import akka.cluster.Cluster
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterSettings
import com.winston.nlp.http.HttpRequestActor
import com.winston.nlp.worker.ReductoActor
import com.winston.nlp.SummaryResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature

class TestApplication(args:Array[String]) extends Bootable {
	val ip = IPTools.getPrivateIp();
	//val config = ConfigFactory.parseString("akka.cluster.roles = [nlp-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"").withFallback(ConfigFactory.load("reducto"))

	val config =
      (if (args.nonEmpty) ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}") else ConfigFactory.empty)
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [nlp-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("reducto"))
      
    val system = ActorSystem("NLPClusterSystem-0-1", config)
    system.log.info("Reducto will start when 2 backend members in the cluster.")
    var frontend: ActorRef = _;
	
    //#registerOnUp
    Cluster(system) registerOnMemberUp {
     frontend = system.actorOf(Props[ReductoActor].withRouter(
    		 ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
    		 ClusterRouterSettings(
    		 totalInstances = 100, maxInstancesPerNode = 1,
    		 allowLocalRoutees = true, useRole = Some("nlp-frontend")))),
    		 name = "ReductoActors")
    }
	
	while (true) {
       val inbox = Inbox.create(system);
       val mapper = new ObjectMapper() with ScalaObjectMapper
       mapper.registerModule(DefaultScalaModule)
       mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
       
       val input = readLine("prompt> ");
       
       if (input.equalsIgnoreCase("exit")) {
        System.exit(0)
       }
       
       else if (input.equalsIgnoreCase("go")){
//         inbox.send(frontend, RawText("Fed maintains strong stimulus as U.S. growth stumbles", "(Reuters) - The Federal Reserve extended its support for a slowing U.S. economy on Wednesday, sounding a bit less optimistic about growth and saying it will keep buying $85 billion in bonds per month for the time being. In announcing the widely expected decision, Fed officials nodded to weaker economic prospects due in part to a fiscal fight in Washington that shuttered much of the government for 16 days earlier this month. The central bank noted that the recovery in the housing market had lost some steam and suggested some frustration at how slowly the labor market was healing. However, it also dropped a phrase expressing concern about a run-up in borrowing costs, suggesting greater comfort with the current level of interest rates. Available data suggest that household spending and business fixed investment advanced, while the recovery in the housing sector slowed somewhat in recent months, the policy-setting Federal Open Market Committee said. Fiscal policy is restraining economic growth. The Fed's statement differed only slightly from the economic assessment it delivered after it last meeting in September, and the reaction in financial markets was relatively subdued. U.S. stocks sold off slightly, while the dollar climbed against the euro and the yen. Prices of U.S. Treasuries turned negative, pushing yields higher."));
//         
//         val SummaryResultContainer(summary) = inbox.receive(500.seconds);
//         val jsonString = mapper.writeValueAsString(summary);
//         println(jsonString)
       }
     }

	def startup ={
	}

	def shutdown={
		system.shutdown;
	}
}

object TestApp{
	def main(args: Array[String]){
		val tester = new TestApplication(args)
	}
}