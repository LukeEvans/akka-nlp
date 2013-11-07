package com.winston.nlp.annotate

import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties
import com.winston.nlp.messages.SplitSentences
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import java.util.ArrayList
import com.winston.nlp.messages.SplitSentences
import com.winston.nlp.messages.SplitSentences

class NLPSplitter {

	var splitProps:Properties = new Properties();
	splitProps.put("annotators", "tokenize, ssplit");
	val splitProcessor:StanfordCoreNLP = new StanfordCoreNLP(splitProps);
	println("--Splitter Created");
	
	def splitProcess(text:String):SplitSentences = {

		var document = new Annotation(text)

		splitProcessor.annotate(document)

		var list = document.get(classOf[SentencesAnnotation])
		var sentences = new ArrayList[String];

		for(m <- list){
		  sentences.add(m.get(classOf[TextAnnotation]))
		}

		SplitSentences(sentences.toList);
	}
}