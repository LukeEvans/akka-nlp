package com.winston.api

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http


object ApiApp{
  def main(args: Array[String]) ={

	implicit val system = ActorSystem("Reducto-Spray-Can-Api")
	
	val service = system.actorOf(Props[ApiActor], "api-service")
	
	IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
	
  }
}
