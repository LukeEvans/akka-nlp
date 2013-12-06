package com.winston.nlp.worker

import com.winston.nlp.annotate.NLPParser
import com.winston.nlp.transport.messages._
import scala.concurrent.duration._
import akka.actor.Actor
import scala.compat.Platform
import scala.util.Random
import com.winston.nlp.NLPSentence
import akka.actor.ActorRef
import scala.concurrent.Future
import akka.util.Timeout
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Await
import scala.util.Success
import scala.util.Failure
import scala.util.control.NonFatal
import akka.actor.Scheduler
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise
import scala.util.Success
import scala.util.Failure

class ParseActor extends Actor {

	val parser = new NLPParser()
	val name = Random.nextInt
	
	override def preStart() {
	  println("--Creating Parser");
      self ! InitRequest
	}
	
	override def postStop() {
		println("--Stopped parser");
	}
	
	def receive = {
	  	case InitRequest => 
	  	  parser.init(); 
		case sc:SentenceContainer =>
		  val origin = sender;
		  processWithTimeout(sc.sentence, origin)
	}
	
	//================================================================================
	// Process with timeout
	//================================================================================
	def processWithTimeout(sentence:NLPSentence, origin:ActorRef) {	
	  import context.dispatcher
	  import akka.pattern.after
	  
	  val sc = parser.parseProcess(sentence)
	  println(sc.sentence.treeString)
	  origin ! sc
	  
	  val start = Platform.currentTime
	  
//	  val delayed = after(200 millis, using = context.system.scheduler)(Future.failed(new Exception("OHNOES")))
//	  val response = Future(parser.parseProcess(sentence))
	  
//	  val result = Future firstCompletedOf Seq(response, delayed)
	  
//	  result map { res =>
//	    println(res)
//	  }
	  
//	  result onComplete {
//	    case Success(sc) => origin ! sc
//	    case Failure(ex) =>
//	      ex.getMessage() match {
//	        case "OHNOES" =>
//	          sentence.putTree("Timeout")
//	          origin ! SentenceContainer(sentence)
//	        case _ => 
//	          println(ex)
//	          sentence.putTree(ex.toString())
//	          origin ! SentenceContainer(sentence)
//	      }
//	  }
	  
//	  val future = Future(parser.parseProcess(sentence))
//	  
//	  Future.firstCompletedOf(Seq(
//			  myafter(1.second, using = context.system.scheduler)(Future.failed(new java.util.concurrent.TimeoutException("ohnoes"))),
//			  future
//)) onComplete { 
//	    case Success(res) => 
//	      println("res")
//	      origin ! res
//	    case Failure(fail) =>
//	      println("fail")
//	      sentence.putTree("fail")
//	      origin ! SentenceContainer(sentence)
//	      fail match {
//	        case e: Exception => e.printStackTrace()
//	        case _ => println("no idea")
//	      }
//	  }
	}
	
 def myafter[T](duration: FiniteDuration, using: Scheduler)(value: ⇒ Future[T])(implicit ec: ExecutionContext): Future[T] =
    if (duration.isFinite() && duration.length < 1) {
      try value catch { case NonFatal(t) => Future.failed(t) }
    } else {
      val p = Promise[T]()
      using.scheduleOnce(duration) { p completeWith { try value catch { case NonFatal(t) ⇒ Future.failed(t) } } }
      p.future
    }
}