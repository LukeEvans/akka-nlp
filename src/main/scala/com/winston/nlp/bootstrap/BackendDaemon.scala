package com.winston.nlp.bootstrap

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable
import com.reactor.nlp.utilities.IPTools
import com.reactor.nlp.config.SystemCreator

class BackendDaemon(args:Array[String]) extends Bootable {
	val ip = IPTools.getPrivateIp();

	println("IP: " + ip)
	
	val config =
      (if (args.nonEmpty) ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}") else ConfigFactory.empty)
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [reducto-backend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("reducto"))
      
    val system = ActorSystem("NLPClusterSystem-0-1", config)
    
	def startup(){
	}

	def shutdown(){
		system.shutdown()
	}
}

object BackendDaemon {
	def main(args:Array[String]){
		var bootstrap = new BackendDaemon(args)
		bootstrap.startup()
		println("Backend node running...")
	}
}