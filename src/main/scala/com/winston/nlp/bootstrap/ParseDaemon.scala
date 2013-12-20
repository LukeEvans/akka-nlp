package com.winston.nlp.bootstrap

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable
import com.reactor.nlp.utilities.IPTools
import com.reactor.nlp.config.SystemCreator

class ParseDaemon extends Bootable {
	val ip = IPTools.getPrivateIp();

	println("IP: " + ip)
	
	val config = ConfigFactory.empty.withFallback(ConfigFactory.parseString("akka.cluster.roles = [reducto-parser]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("reducto"))
      
    val system = ActorSystem("NLPClusterSystem-0-1", config)
    
	def startup(){
	}

	def shutdown(){
		system.shutdown()
	}
}

object ParseDaemon {
	def main(args:Array[String]){
		var parseDaemon = new ParseDaemon
		parseDaemon.startup()
		println("Parse node running...")
	}
}