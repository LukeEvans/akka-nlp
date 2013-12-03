package com.winston.nlp.annotate

import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import java.util.ArrayList
import com.winston.nlp.transport.messages._
import com.winston.nlp.SentenceSet
import com.winston.nlp.NLPSentence
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import com.winston.nlp.transport.ReductoRequest
import com.winston.webextraction.WebExtractor

class NLPSplitter {

	var splitProps:Properties = new Properties();
	splitProps.put("annotators", "tokenize, ssplit");
	var splitProcessor:StanfordCoreNLP = null;
	
	def init() {
		splitProcessor = new StanfordCoreNLP(splitProps)
		println("--Splitter Created");
	}
	
	def splitProcess(request: ReductoRequest):SetContainer = {
	  
		var document:Annotation = null
		var set:SentenceSet = null
		
		if(request.headline != null && request.text != null){
			set = new SentenceSet(request.headline, request.text);		
			document = new Annotation(request.text)
		}
		else if(request.url != null){
			var articleExtractor = new WebExtractor()
			var (headline, text) = articleExtractor.getExtraction(request.url)
			set = new SentenceSet(headline, text)
			document = new Annotation(text)		
		}
		else{
		  return null
		}

		splitProcessor.annotate(document)

		var list = document.get(classOf[SentencesAnnotation])

		for(m <- list){
		  
		  var sentence = new NLPSentence(m.get(classOf[TextAnnotation]));
		  
		  for (t <- m.get(classOf[TokensAnnotation])) {
			  sentence.addWord(t.get(classOf[TextAnnotation]), t.beginPosition(), t.endPosition(), t.originalText());
		  }
		  
		  set.addSentence(sentence);
		}

		SetContainer(set, 0);
	}
}