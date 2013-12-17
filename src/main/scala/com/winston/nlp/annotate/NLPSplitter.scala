package com.winston.nlp.annotate

import java.io.FileInputStream
import java.io.InputStream
import java.util.Properties
import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import com.winston.nlp.NLPSentence
import com.winston.nlp.SentenceSet
import com.winston.nlp.transport.ReductoRequest
import com.winston.nlp.transport.messages._
import com.winston.nlp.worker.URLExtractorActor
import akka.actor._
import akka.cluster.routing.ClusterRouterConfig
import akka.cluster.routing.ClusterRouterSettings
import akka.pattern.ask
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import akka.event.LoggingAdapter
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import opennlp.tools.tokenize.Tokenizer
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import akka.pattern.CircuitBreaker
import akka.pattern.CircuitBreakerOpenException
import com.winston.nlp.transport.ErrorResponse
import scala.concurrent.Future

class NLPSplitter{
	var currentPath = System.getProperty("user.dir")
	var splitProps:Properties = new Properties();
	splitProps.put("annotators", "tokenize, ssplit");
	var splitProcessor:StanfordCoreNLP = null;
	var tokenizer:Tokenizer = null;
	var tokenModelIn:InputStream = new FileInputStream(currentPath + "/src/main/resources/en-token.bin");
	var tokenModel:TokenizerModel = null
	
	def init(system:akka.actor.ActorSystem) {
		splitProcessor = new StanfordCoreNLP(splitProps)
		println("--Splitter Created");
		tokenModel = new TokenizerModel(tokenModelIn);
		tokenizer = new TokenizerME(tokenModel);
		tokenModelIn.close()
	}
	
	def splitProcess(request: ReductoRequest):SetContainer = {
			var document:Annotation = null
			var set:SentenceSet = null
		
			if(request.headline != null && request.text != null){
				set = new SentenceSet(request.headline, request.text);		
				document = new Annotation(request.text)
			}
			else return null
				
			splitProcessor.annotate(document)			
			var list = document.get(classOf[SentencesAnnotation])
						
			for(m <- list){
				
				var sentence = new NLPSentence(m.get(classOf[TextAnnotation]));					  
				val spans = tokenizer.tokenizePos(sentence.value)
				val tokens = tokenizer.tokenize(sentence.value)
							
				for(idx <- 0 to spans.length-1){
					val span = spans(idx);
					sentence.addWord(tokens(idx), span.getStart(), span.getEnd(), tokens(idx))
				}
				set.addSentence(sentence);
			}			
			SetContainer(set, 0);    
	}
}