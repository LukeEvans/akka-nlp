package com.winston.nlp.worker

import com.winston.nlp.annotate.NLPSplitter
import com.winston.nlp.transport.messages._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.winston.nlp.transport.ReductoRequest
import scala.concurrent.Future
import akka.pattern.CircuitBreaker
import com.winston.nlp.transport.ErrorResponse

class SplitActor(urlExtractorRouter:ActorRef) extends Actor {

	val splitter = new NLPSplitter;
	var breaker:CircuitBreaker = null

	override def preStart() {
	  println("--Creating splitter");
	  breaker = new CircuitBreaker(context.system.scheduler,
	      maxFailures = 5,
	      callTimeout = (0).seconds,
	      resetTimeout = 1.minute)
      self ! InitRequest
	}

	override def postStop() {
		println("--Stopped splitter");
	}
	
	def receive = {	  
	  	case InitRequest => splitter.init(context.system); 
		case RequestContainer(request) => {
		  try{
			  val origin = sender
			  processSplit(origin, request)
		  }catch{
		    case e:Exception => {
		      e.printStackTrace()
		      sender ! CircuitBreakException(new ErrorResponse("URL Text Extraction Circuit Breaker Open"))
		    }
		  }
		}
	}
	
	def processSplit(origin:ActorRef, request:ReductoRequest){
	  implicit val timeout = Timeout(5 seconds);
	  
	  var response = for{
					extraction <-(urlExtractorRouter ? breaker.withSyncCircuitBreaker(URLContainer(request.url))).mapTo[URLTextResponse]
	  }yield extraction
				
	  response map{
		  extractionContainer =>
		  	request.headline = extractionContainer.extractionTuple._1
		  	request.text = extractionContainer.extractionTuple._2
		  	origin ! splitter.splitProcess(request)
		}
	}
}