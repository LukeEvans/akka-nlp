package com.winston.nlp.search

import akka.actor.Actor
import akka.actor.ActorRef
import org.elasticsearch.common.settings.ImmutableSettings
import com.winston.nlp.http.HttpRequestActor
import java.util.ArrayList
import com.winston.nlp.messages._

class ElasticSearchActor extends HttpRequestActor {
	
	val totalCount = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/news,twitter/_count";
	val queryCount = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/news,twitter/_count?q=text:";
	val stopPhrases = "http://ec2-54-234-94-194.compute-1.amazonaws.com:9200/stop/_search?size=500";
	var recCount:Int = 0;
	var sentCount:Int = 0;
	
	override def receive = {
		case term: SingleTermFrequency =>
		  val origin = sender;
		  processTermSearch(term.word, origin);
		 
		case l:LongContainer => 
		  val origin = sender;
		  processTotalDocuments(origin);
		  
		case sp:StopPhrasesObject =>
		  val origin = sender;
		  processStopPhrases(origin);
		  
	}

	def processTermSearch(text: String, origin:ActorRef) {
		val uri = queryCount + text
		recCount += 1;
		val node = processRequest(HttpObject(uri, null, null, "GET"), null)
		val freq = SingleTermFrequency(text, node.path("count").asLong());
		origin ! freq
		sentCount += 1;
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