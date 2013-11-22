package com.winston.nlp.annotate

import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import java.util.ArrayList
import com.winston.nlp.messages._
import com.winston.nlp.SentenceSet
import com.winston.nlp.messages.RawText
import com.winston.nlp.NLPSentence
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;

class NLPSplitter {

	var splitProps:Properties = new Properties();
	splitProps.put("annotators", "tokenize, ssplit");
	var splitProcessor:StanfordCoreNLP = null;
	
	def init() {
		splitProcessor = new StanfordCoreNLP(splitProps)
		println("--Splitter Created");
	}
	
	def splitProcess(textObject: RawText):SetContainer = {

		var set = new SentenceSet(textObject.query, textObject.text);
		
		var document = new Annotation(textObject.text)

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