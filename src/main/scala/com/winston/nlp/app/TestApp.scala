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
import com.reactor.nlp.config.SystemCreator
import com.winston.nlp.worker.SplitActor
import akka.routing.FromConfig
import com.winston.nlp.worker.NLPActor
import com.winston.nlp.worker.ComboActor
import com.winston.nlp.worker.NLPActor
import com.winston.nlp.messages._
import com.winston.nlp.worker.ParseActor
import akka.actor.Inbox
import scala.concurrent.duration._
import com.winston.utlities.Tools
import akka.serialization.SerializationExtension
import akka.cluster.Cluster
import com.winston.nlp.worker.NLPActor
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.AdaptiveLoadBalancingRouter
import akka.cluster.routing.ClusterRouterSettings
import com.winston.nlp.http.HttpRequestActor

class TestApplication extends Bootable {
	val ip = IPTools.getPrivateIp();
	val config = ConfigFactory.parseString("akka.cluster.roles = [nlp-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"").withFallback(ConfigFactory.load("reducto"))

    val system = ActorSystem("NLPClusterSystem-0-1", config)
    system.log.info("Reducto will start when 2 backend members in the cluster.")
    var frontend: ActorRef = _;
	
//	val reqActor = system.actorOf(Props(classOf[HttpRequestActor]), name = "http");
//	reqActor ! HttpObject("http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/news,twitter/_count?q=text:obama",null,null,"GET");
//	val inbox = Inbox.create(system);
//	inbox.send(reqActor, HttpObject("http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/news,twitter/_count?q=text:obama",null,null,"GET"))
//	val HttpObject(uri, obj, response, method) = inbox.receive(5.seconds)
//	
//	println(response)
	
    //#registerOnUp
    Cluster(system) registerOnMemberUp {
     frontend = system.actorOf(Props[NLPActor].withRouter(
    		 ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
    		 ClusterRouterSettings(
    		 totalInstances = 100, maxInstancesPerNode = 1,
    		 allowLocalRoutees = true, useRole = Some("nlp-frontend")))),
    		 name = "ReductoActors")
    }
	
	while (true) {
       val inbox = Inbox.create(system);
       val input = readLine("prompt> ");
       
       if (input.equalsIgnoreCase("exit")) {
        System.exit(0)
       }
       
       else {
         inbox.send(frontend, RawText("Fake Query", input));
         val SetContainer(set) = inbox.receive(500.seconds);
         
         set.sentences.toList map { sentence =>
           println(sentence.tree)
         }
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
		val tester = new TestApplication
	}
}