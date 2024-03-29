package com.winston.nlp.annotate

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation
import java.util.Properties
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.Annotation
import java.util.ArrayList
import com.winston.nlp.NLPSentence
import com.winston.nlp.messages.SentenceContainer
import com.winston.nlp.messages.SentenceContainer



class NLPParser {
	var parseProps = new Properties()
	parseProps.put("annotators", "tokenize, ssplit, pos, parse")
	var parseProcessor:StanfordCoreNLP = null;
	
	def init() {
	  parseProcessor = new StanfordCoreNLP(parseProps)
	  println("--Parser Created");
	}
	
	def parseProcess(sentence:NLPSentence): SentenceContainer = {

		var document = new Annotation(sentence.value);

		parseProcessor.annotate(document)

		var list = document.get(classOf[SentencesAnnotation])
		var trees = new ArrayList[String];

		for(m <- list){
			trees.add(m.get(classOf[TreeAnnotation]).toString());
		}

		if (trees.size() > 0) {
			sentence.putTree(trees.get(0));
		} 
		
		SentenceContainer(sentence)
	}
}