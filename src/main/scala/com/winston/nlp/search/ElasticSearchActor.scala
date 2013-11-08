package com.winston.nlp.search

import akka.actor.Actor
import com.winston.nlp.messages.SingleTermFrequency
import akka.actor.ActorRef
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.winston.nlp.messages.SingleTermFrequency
import org.elasticsearch.common.settings.ImmutableSettings

class ElasticSearchActor extends Actor {

  
	val settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
	val client = ElasticClient.remote(settings, ("ec2-54-211-99-5.compute-1.amazonaws.com", 9300));

//	val client = ElasticClient.remote("ec2-54-234-94-194.compute-1.amazonaws.com" -> 9300);
	println("ES Client connected");

	def receive = {
		case term: SingleTermFrequency => processTermSearch(term.word, sender);
	}

	def processTermSearch(text: String, origin:ActorRef) {
//		val resp = client.sync.execute {
//		  search in "places"->"cities" query "London"
////			count from "news twitter" query { term("text", text) }
//		}
//		
//		println(resp)
//		
//		val c = resp.asInstanceOf[Long];
		
	  val c = 56;
	  println("sending back: " + c)
	  origin ! SingleTermFrequency(text,c);
	}

}