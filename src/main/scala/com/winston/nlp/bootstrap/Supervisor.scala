package com.winston.nlp.bootstrap

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable
import com.reactor.nlp.utilities.IPTools
import com.reactor.nlp.config.SystemCreator
import akka.actor.Props
import akka.cluster.Cluster
import com.winston.nlp.listener.Listener
import akka.cluster.ClusterEvent.ClusterDomainEvent

class Supervisor extends Bootable {
	val ip = IPTools.getPrivateIp();

	println("IP: " + ip)
	
    val config = ConfigFactory.parseString("akka.cluster.roles = [reducto-supervisor]\nakka.remote.netty.tcp.hostname=\""+ip+"\"").withFallback(ConfigFactory.load("reactor"))
        
    val system = ActorSystem("NLPClusterSystem-0-2", config)
    
    println("Supervisor node running...")	  
    
	def startup(){
		val clusterListener = system.actorOf(Props(classOf[Listener], system),
             name = "clusterListener")
         Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
	}

	def shutdown(){
		system.shutdown()
	}
}

object Supervisor {
	def main(args:Array[String]){
		var supervisor = new Supervisor
		supervisor.startup()
	}
}