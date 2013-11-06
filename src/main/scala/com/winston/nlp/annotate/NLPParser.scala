package com.winston.nlp.annotate

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation
import java.util.Properties
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.pipeline.Annotation



class NLPParser {
	var parseProperties = new Properties()
	parseProperties.put("annotators", "tokenize, ssplit, pos, parse")
	val parseProcessor:StanfordCoreNLP = new StanfordCoreNLP(parseProperties)
}