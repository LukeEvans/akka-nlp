package com.winston.monitoring

import akka.actor.Actor
import com.timgroup.statsd.NonBlockingStatsDClient

abstract class MonitoredActor(tag:String) extends Actor {

  val tags:Array[String] = Array("tag:" + tag)
  
  // Datadog client
  val statsd = new NonBlockingStatsDClient(
    "reducto-1", 				                    /* prefix to any stats; may be null or empty string */
    "localhost",					    			/* common case: localhost */
    8125,   		                                /* port */
    tags								            /* DataDog extension: Constant tags, always applied */
  )
}