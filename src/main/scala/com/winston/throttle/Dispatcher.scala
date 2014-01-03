package com.winston.throttle

import akka.actor.Actor
import com.winston.nlp.transport.messages.RequestContainer
import scala.compat.Platform
import akka.actor.ActorRef
import akka.actor.Props
import spray.routing.RequestContext
import com.fasterxml.jackson.databind.ObjectMapper
import spray.routing.RequestContext
import com.winston.api.PerRequestActor
import com.winston.nlp.transport.messages.Error
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.routing.RequestContext
import javax.naming.ServiceUnavailableException
import com.winston.nlp.transport.messages._
import com.winston.monitoring.MonitoredActor

class Dispatcher(reductoRouter:ActorRef) extends MonitoredActor("reducto-dispatcher"){

  def receive = {
    case DispatchRequest(request, ctx, mapper) => 
         val start = Platform.currentTime
         val tempActor = context.actorOf(Props(classOf[PerRequestActor], start, ctx, mapper))
        	
        reductoRouter.tell(request, tempActor)
        log.info("Handling request")
    
    case OverloadedDispatchRequest(message) =>
        message match {
          case req:DispatchRequest =>
          	val err = req.mapper.writeValueAsString(Error("Rate limit exceeded"))
          	completeOverload(req.ctx, ServiceUnavailable, err)    
          	log.error(err)
          	
          case _ => log.info("Unrecognized overload message")
        }

  }
  
  // Handle the completing of Responses
  def completeOverload(ctx: RequestContext, status: StatusCode, obj: String) = {
   	ctx.complete(status, obj)
  }
}