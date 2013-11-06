package com.winston.nlp.app

import akka.kernel.Bootable
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import com.winston.nlp.annotate._
import com.reactor.nlp.utilities.IPTools
import com.reactor.nlp.config.SystemCreator
import com.winston.nlp.worker.SplitActor
import akka.routing.FromConfig
import com.winston.nlp.worker.NLPActor
import com.winston.nlp.worker.NLPActor
import com.winston.nlp.messages.RawText


class TestApplication extends Bootable {
	val ip = IPTools.getPrivateIp();
	val port = "2554";
	
	val system = SystemCreator.createClientSystem("System", ip, port);
	
	val splitRouter = system.actorOf(Props(classOf[SplitActor]).withRouter(new FromConfig()), "splitWorkers");
	val nlpWorker = system.actorOf(Props(classOf[NLPActor], splitRouter).withRouter(new FromConfig()), "nlpWorkers");

	def sendText(text: String) = {
		println("Sending Text: " + text);
		nlpWorker ! RawText(text);
	}
	
	def startup ={

	}

	def shutdown={
			system.shutdown
	}
}

object TestApp{
	def main(args: Array[String]){
		val tester = new TestApplication
		
		Thread sleep 3000
		tester.sendText("Hello there, my name is Luke. What is your name? Wow, that's a stupid name");
	}
}