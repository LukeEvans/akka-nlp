package com.winston.nlp.bootstrap

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable
import com.reactor.nlp.utilities.IPTools
import com.reactor.nlp.config.SystemCreator
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.winston.nlp.listener.Listener

class SeedDaemon extends Bootable {
	val ip = IPTools.getPrivateIp();

	println("IP: " + ip)
	
    val config = ConfigFactory.parseString("akka.cluster.roles = [reducto-seed]\nakka.remote.netty.tcp.port=2551\nakka.remote.netty.tcp.hostname=\""+ip+"\"").withFallback(ConfigFactory.load("reducto"))
        
    val system = ActorSystem("NLPClusterSystem-0-1", config)
    
    println("Seed node running...")	  
    
	def startup(){
		val clusterListener = system.actorOf(Props(classOf[Listener], system),
             name = "clusterListener")
         Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
	}

	def shutdown(){
		system.shutdown()
	}
}

object SeedDaemon {
	def main(args:Array[String]){
	  val seed = new SeedDaemon
	}
}