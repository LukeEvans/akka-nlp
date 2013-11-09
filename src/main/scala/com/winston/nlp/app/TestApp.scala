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
import com.winston.nlp.worker.ComboActor
import com.winston.nlp.messages.RawText
import com.winston.nlp.worker.ParseActor
import akka.actor.Inbox
import com.winston.nlp.messages.RawText
import scala.concurrent.duration._
import com.winston.nlp.messages.ScoreRequest
import com.winston.utlities.Tools


class TestApplication extends Bootable {
	val ip = IPTools.getPrivateIp();
	val port = "2554";
	
	val system = SystemCreator.createClientSystem("System", ip, port);
	
	val splitRouter = system.actorOf(Props(classOf[SplitActor]).withRouter(new FromConfig()), "splitWorkers");
	val parseRouter = system.actorOf(Props(classOf[ParseActor]).withRouter(new FromConfig()), "parseWorkers");
	val nlpWorker = system.actorOf(Props(classOf[NLPActor], splitRouter, parseRouter).withRouter(new FromConfig()), "nlpWorkers");
	val comboWorker = system.actorOf(Props(classOf[ComboActor]).withRouter(new FromConfig()), "comboWorkers");

	Thread sleep 10000
	val inbox = Inbox.create(system);
	//inbox.send(nlpWorker, RawText("Hello there, my name is Luke. What is your name? Wow, that's a stupid name"));
	//val ScoreRequest(text, response) = inbox.receive(5.seconds);
	
	inbox.send(comboWorker, "Test message")
	
	//println(response)
	
	
	def startup ={

	}

	def shutdown={
		system.shutdown;
	}
}

object TestApp{
	def main(args: Array[String]){
		//val tester = new TestApplication
		println(Tools.leafIsLeadingSymbol("$"))
	}
}