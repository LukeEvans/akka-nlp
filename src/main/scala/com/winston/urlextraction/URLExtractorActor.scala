package com.winston.urlextraction

import akka.actor._
import akka.pattern.Patterns
import akka.util.Timeout
import com.winston.nlp.transport.messages._

class URLExtractorActor extends Actor {
	    val urlExtractor = new URLExtractor()

        override def preStart() {
          println("--Creating URLExtractor");
        }

        override def postStop() {
                println("--Stopped splitter");
        }
        
        def receive = {
                case URLContainer(url) => sender ! new URLTextResponse(urlExtractor.getExtraction(url))
        }
}