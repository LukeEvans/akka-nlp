package com.winston.api

import akka.actor.Actor
import akka.actor.ActorRef
import spray.routing.RequestContext
import scala.concurrent.duration._
import spray.http.StatusCodes._
import com.winston.nlp.transport.messages.ResponseContainer
import spray.http.StatusCode
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Stop
import akka.actor.ActorLogging
import com.fasterxml.jackson.databind.ObjectMapper
import akka.actor.ReceiveTimeout

class PerRequestActor(startTime: Long, ctx: RequestContext, mapper: ObjectMapper) extends Actor with ActorLogging {
    
    case class Error(status: String)
    
    import context._
    
	setReceiveTimeout(20.seconds)
  
	def receive = {
		case ResponseContainer(response) =>
		  complete(OK, response.finishResponse(startTime, mapper))
		case ReceiveTimeout => 
		  val error = Error("Request timeout")
		  val errString = mapper.writeValueAsString(error)
		  log.error(errString)
		  complete(GatewayTimeout, errString)
		case _ => 
		  log.error("Got a message that I've never even heard of!")
		  stop(self)
	}
	
    // Handle the completing of Responses
    def complete(status: StatusCode, obj: String) = {
    	ctx.complete(status, obj)
    	stop(self)
    }
    
    // Supervisor Strategy
    override val supervisorStrategy =
     OneForOneStrategy() {
      case e => {
        log.error(e.getMessage)
        complete(InternalServerError, e.getMessage)
        Stop
      }
    }
}