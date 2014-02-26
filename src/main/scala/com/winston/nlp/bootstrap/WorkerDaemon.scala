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

class WorkerDaemon extends Bootable {
	val ip = IPTools.getPrivateIp();

	println("IP: " + ip)
	
	val config = ConfigFactory.empty.withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("reactor"))
      
    val system = ActorSystem("NLPClusterSystem-0-2", config)
    
	def startup(){
		 val clusterListener = system.actorOf(Props(classOf[Listener], system),
             name = "clusterListener")
         Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
	}

	def shutdown(){
		system.shutdown()
	}
}

object WorkerDaemon {
	def main(args:Array[String]){
		var workerDaemon = new WorkerDaemon
		workerDaemon.startup()
		println("Worker node running...")
	}
}