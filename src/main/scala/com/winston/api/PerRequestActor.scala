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
import com.winston.monitoring.MonitoredActor
import scala.compat.Platform
import com.winston.nlp.transport.messages.Error

class PerRequestActor(startTime: Long, ctx: RequestContext, mapper: ObjectMapper) extends MonitoredActor("per-request-actor") with ActorLogging {
    
    import context._
    
    // Increment count for per request actors
    statsd.count("per-requst-actors", 1)
    
	setReceiveTimeout(2.seconds)
  
	def receive = {
		case ResponseContainer(response) =>
		  complete(OK, response.finishResponse(startTime, mapper))
		case ReceiveTimeout => 
		  val error = Error("Request timeout")
		  val errString = mapper.writeValueAsString(error)
		  log.error(errString)
		  complete(OK, errString)
		  statsd.histogram("request.timeout", 1)
		  
		case _ => 
		  log.error("Got a message that I've never even heard of!")
		  statsd.histogram("unrecognized.message", 1)
		  stop(self)
	}
	
    // Handle the completing of Responses
    def complete(status: StatusCode, obj: String) = {
    	ctx.complete(status, obj)
    	
    	// Push time to datadog
    	statsd.histogram("response.time", Platform.currentTime - startTime)
    	statsd.count("per-requst-actors", -1)
    	
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