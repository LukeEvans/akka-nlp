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
	
	val totalCount = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/news,twitter/_count";
	val queryCount = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/news,twitter/_count?q=text:";
	val stopPhrases = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/stop/_search?size=500";

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
	}
	
	def processTermSearch(text: String, origin:ActorRef) {
		val uri = queryCount + text
		val node = processRequest(HttpObject(uri, null, null, "GET"), null)
		val freq = SingleTermFrequency(text, node.path("count").asLong());
		origin ! freq
	}

	def processTotalDocuments(origin:ActorRef) {
		val uri = totalCount;
		val node = processRequest(HttpObject(uri), null)
		origin ! LongContainer(node.path("count").asLong())
	}
	
	def processStopPhrases(origin:ActorRef) {
		val uri = stopPhrases;
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
	}
}