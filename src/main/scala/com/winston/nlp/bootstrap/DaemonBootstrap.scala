package com.winston.nlp.bootstrap

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable
import com.reactor.nlp.utilities.IPTools
import com.reactor.nlp.config.SystemCreator

class Daemon extends Bootable {
	val ip = IPTools.getPrivateIp();
	val port = "2552";
	val system = SystemCreator.createBaseSystem("DaemonSystem", ip, port);

	def startup(){
	}

	def shutdown(){
		system.shutdown()
	}

}

object DaemonBootstrap {
	def main(args:Array[String]){
		var bootstrap = new Daemon()
		bootstrap.startup()
		println("Daemon Application running...")
	}
}