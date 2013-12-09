package com.winston.nlp.search

import akka.actor.Actor
import akka.actor.ActorRef
import org.elasticsearch.common.settings.ImmutableSettings
import com.winston.nlp.http.HttpRequestActor
import java.util.ArrayList
import com.winston.nlp.transport.messages._
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import java.util.LinkedHashMap
import org.elasticsearch.action.ActionFuture
import org.elasticsearch.action.count.CountResponse
import scala.collection.JavaConversions._

class ElasticSearchActor extends HttpRequestActor {
	
	val totalCountEndpoint = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/news,twitter/_count";
	val queryCountEndpoint = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/news,twitter/_count?q=text:";
	val stopPhraseEndpoint = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/stop/_search?size=500";
	
	var stopRequestsProcessed = 0;
	var tdRequestsProcessed = 0;
	val refreshThreshold = 1000;
	
	var stopPhrases = new ArrayList[String];
	var totalDocuments: Long = 0;
	
	
	// Elasticsearch client 
	var client: Client = null;
	
	override def preStart() {
	  println("--Creating ES Bulker");
	  self ! InitRequest
	}
	
	override def postStop() {
	  println("--Stopped ES Bulker");
	}
	
	override def receive = {
	    case InitRequest => init()
	    
		case term: SingleTermFrequency =>
		  val origin = sender;
		  processTermSearch(term.word, origin);
		  
		case TermFrequencyBulkReq(list) =>
	  	  val origin = sender
	  	  processBulk(list, origin)
	  	  
		case l:LongContainer => 
		  val origin = sender;
		  processTotalDocuments(origin);
		  
		case sp:StopPhrasesObject =>
		  val origin = sender;
		  processStopPhrases(origin);
		  
	}
	//================================================================================
	// Init 
	//================================================================================
	def init() {
		val settings = ImmutableSettings.settingsBuilder().put("cluster.name", "es_sg").put("client.transport.ping_timeout", "120s").build();
		client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("ec2-54-211-99-5.compute-1.amazonaws.com", 9300));
		println("--ES Bulker Created");
	}

	//================================================================================
	// Process bulk
	//================================================================================
	def processBulk(wordList: List[String], origin: ActorRef) {
		val pending: LinkedHashMap[String, ActionFuture[CountResponse]] = new LinkedHashMap[String, ActionFuture[CountResponse]]();
	  
		// Send off all to execute
		wordList map { word =>
		  if (!pending.containsKey(word.toLowerCase())) {
		    pending.put(word.toLowerCase(), client.prepareCount("news", "twitter").setQuery(QueryBuilders.queryString(word).defaultField("text")).execute())
		  }
		}
		
		val wordMap: LinkedHashMap[String, Long] = new LinkedHashMap[String, Long]();

		// Collect all
		wordList map { word =>
		  if (pending.containsKey(word.toLowerCase())) {
		    wordMap.put(word, pending.get(word.toLowerCase()).actionGet().getCount())
		  }
		}
		
		origin ! TermFrequencyResponse(wordMap.toMap)
	  
//	  val wordMap: LinkedHashMap[String, Long] = new LinkedHashMap[String, Long]();
//	  wordList map { word => wordMap.put(word,1)}
//	  origin ! TermFrequencyResponse(wordMap.toMap)
	}
	
	def processTermSearch(text: String, origin:ActorRef) {
		val uri = queryCountEndpoint + java.net.URLEncoder.encode(text, "UTF-8")
		val node = processRequest(HttpObject(uri, null, null, "GET"), null)
		val freq = SingleTermFrequency(text, node.path("count").asLong());
		origin ! freq
	}

	def processTotalDocuments(origin:ActorRef) {
	  	// If we've processed (refreshThreshold), refresh total docs
	    if (tdRequestsProcessed < refreshThreshold && totalDocuments > 0) {
	    	origin ! LongContainer(totalDocuments)
	    	return;
	    }
	    
		val uri = totalCountEndpoint;
		val node = processRequest(HttpObject(uri), null)
		val newCount = node.path("count").asLong()
		origin ! LongContainer(newCount)
		
		// Reset
		totalDocuments = newCount
		tdRequestsProcessed = 0;
	}
	
	def processStopPhrases(origin:ActorRef) {
	  
	    // If we've processed (refreshThreshold), refresh stop list
	    if (stopRequestsProcessed < refreshThreshold && stopPhrases != null && stopPhrases.size() > 0) {
	    	origin ! StopPhrasesObject(stopPhrases)
	    	return;
	    }
	    
		val uri = stopPhraseEndpoint;
		val node = processRequest(HttpObject(uri), null)
				
		val phrases = new ArrayList[String];
		
		val it = node.path("hits").path("hits").iterator()
		while (it.hasNext()) {

		  try {
			  val hit = it.next();
		  
			  val phrase = hit.path("_source").path("title").asText().toLowerCase();
			  if (!phrases.contains(phrase)) {
				  phrases.add(phrase)
			  }
		    
		  } 
		} 
		  
		origin ! StopPhrasesObject(phrases)
		
		// Reset phrases
		stopPhrases = phrases
		stopRequestsProcessed = 0;
	}
}