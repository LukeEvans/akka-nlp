package com.winston.nlp.bootstrap

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable
import com.reactor.nlp.utilities.IPTools
import com.reactor.nlp.config.SystemCreator

class ParseDaemon(args:Array[String]) extends Bootable {
	val ip = IPTools.getPrivateIp();

	println("IP: " + ip)
	
    val config = ConfigFactory.parseString("akka.cluster.roles = [reducto-parse]\nakka.remote.netty.tcp.hostname=\""+ip+"\"").withFallback(ConfigFactory.load("reducto"))
        
    val system = ActorSystem("NLPClusterSystem-0-1", config)
    
    println("Parse node running...")	  
    
	def startup(){
	}

	def shutdown(){
		system.shutdown()
	}
}

object ParseDaemon {
	def main(args:Array[String]){
	  
	  var nodeNumber = 1
	  
	  if (args.length > 0) {
	    nodeNumber = args(0).toInt
	  }
	  
	  for (i <- 0 to nodeNumber - 1) {
		  var bootstrap = new ParseDaemon(args)
		  bootstrap.startup()
	  }
	}
}