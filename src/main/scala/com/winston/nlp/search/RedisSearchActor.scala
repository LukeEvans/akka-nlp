package com.winston.nlp.search

import akka.actor.Actor
import com.winston.nlp.transport.messages.LongContainer
import com.winston.nlp.transport.messages.TermFrequencyBulkReq
import com.winston.nlp.transport.messages.InitRequest
import com.winston.nlp.transport.messages.StopPhrasesObject
import com.winston.nlp.transport.messages.SingleTermFrequency
import scala.concurrent.duration._
import redis.clients.jedis.Jedis
import akka.actor.ActorRef
import scala.collection.JavaConversions._
import java.util.ArrayList
import java.util.LinkedHashMap
import com.winston.nlp.transport.messages.TermFrequencyResponse

class RedisSearchActor extends Actor {

  	var totalDocuments: Long = 0;
  	var stopPhrases = new ArrayList[String];
  	
	val jedis = new Jedis("localhost");
  	
  	implicit val ec = context.dispatcher
  	case object RefreshTick
	val cancellable =
		context.system.scheduler.schedule(0 seconds,
		60 minutes,
		self,
		RefreshTick)
    
  	override def receive = {
  	    case RefreshTick => refresh
  	    
		case TermFrequencyBulkReq(list) =>
	  	  val origin = sender
	  	  processBulk(list, origin)
	  	  
		case l:LongContainer => 
		  sender ! LongContainer(totalDocuments)
		  
		case sp:StopPhrasesObject => 
		  sender ! StopPhrasesObject(stopPhrases)
	}
  	
  	def refresh() {
  		try{
	  		// Refresh stop phrases
	  		val stops = jedis.lrange("stop_list", 0, -1)
	  		stopPhrases.clear()
	  		
	  		stops map { word =>
	  			stopPhrases.add(word)
	  		}
	  		
	  		// Refresh Total docs
	  		totalDocuments = jedis.get("reducto-total-docs").toLong
  		}catch{
  		case e:Exception =>
  		    e.printStackTrace()
  		}
  	}
  	
  	def processBulk(words:List[String], origin:ActorRef) {
  		val temp = new ArrayList[String]
  	    words map { w =>
  			temp.add(w.toLowerCase())
  		}

  		val lowerWords = temp.toList
  		val counts = jedis.mget(lowerWords:_*)
  		
  		val wordMap = combineCountsKeys(counts.toList, lowerWords)
  		origin ! TermFrequencyResponse(wordMap.toMap)
  	}
  	
  	// Combines keys and values
  	def combineCountsKeys(counts:List[String], words:List[String]): LinkedHashMap[String, Long] = {
  	  val wordMap: LinkedHashMap[String, Long] = new LinkedHashMap[String, Long]();
  	  
  	  if (counts.size != words.size) return wordMap
  	  
  	  for (i <- 0 until counts.size) {
  		  val w = words.get(i)
  		  val c = counts.get(i)
  		  var longCount:Long = 0
  		  
  		  try {
  			  longCount = c.toLong
  		  } catch {
  		    case e:Exception => longCount = 0
  		  }
  		  
  		  wordMap.put(w, longCount)
  	  }
  	  
  	  wordMap
  	}
}