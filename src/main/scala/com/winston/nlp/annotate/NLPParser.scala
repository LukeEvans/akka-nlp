package com.winston.nlp.annotate

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation
import java.util.Properties
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.Annotation
import com.winston.nlp.messages.ParsedSentence
import com.winston.nlp.messages.SplitSentences
import java.util.ArrayList
import com.winston.nlp.messages.ParsedSentence



class NLPParser {
	var parseProperties = new Properties()
	parseProperties.put("annotators", "tokenize, ssplit, pos, parse")
	val parseProcessor:StanfordCoreNLP = new StanfordCoreNLP(parseProperties)
	println("--Parser Created");
	
	def parseProcess(text:String): ParsedSentence = {

		var document = new Annotation(text)

		parseProcessor.annotate(document)

		var list = document.get(classOf[SentencesAnnotation])
		var trees = new ArrayList[String];

		for(m <- list){
			trees.add(m.get(classOf[TreeAnnotation]).toString());
		}

		if (trees.size() == 0) {
		  return ParsedSentence(text, null);
		} 
		
		ParsedSentence(text, trees.get(0));
	}
}