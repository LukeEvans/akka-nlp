package com.winston.nlp.search

import akka.actor.Actor
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

class ElasticSearchActor extends Actor {

	val settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
	val client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("ec2-54-234-94-194.compute-1.amazonaws.com", 9300));

	def receive = {
		case sentence: String => sender ! sentence
	}


}