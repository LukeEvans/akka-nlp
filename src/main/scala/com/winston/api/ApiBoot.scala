package com.winston.api

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http


class ApiBoot extends App {
	implicit val system = ActorSystem("on-spray-can")
	
	val service = system.actorOf(Props[ApiActor], "demo-service")
	
	IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
	while(true){
	  
	}
}

object ApiApp{
  def main(args: Array[String]) ={
    val boot = new ApiBoot
  }
}
