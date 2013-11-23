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
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import com.winston.nlp.transport.ReductoRequest

class NLPSplitter {

	var splitProps:Properties = new Properties();
	splitProps.put("annotators", "tokenize, ssplit");
	var splitProcessor:StanfordCoreNLP = null;
	
	def init() {
		splitProcessor = new StanfordCoreNLP(splitProps)
		println("--Splitter Created");
	}
	
	def splitProcess(request: ReductoRequest):SetContainer = {

		var set = new SentenceSet(request.headline, request.text);
		
		var document = new Annotation(request.text)

		splitProcessor.annotate(document)

		var list = document.get(classOf[SentencesAnnotation])

		for(m <- list){
		  
		  var sentence = new NLPSentence(m.get(classOf[TextAnnotation]));
		  
		  for (t <- m.get(classOf[TokensAnnotation])) {
			  sentence.addWord(t.get(classOf[TextAnnotation]), t.beginPosition(), t.endPosition());
		  }
		  
		  set.addSentence(sentence);
		}

		SetContainer(set);
	}
}