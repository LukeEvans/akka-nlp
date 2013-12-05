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
import com.winston.nlp.transport.messages._
import java.io.ByteArrayInputStream
import scala.io.Source
import edu.stanford.nlp.trees.PennTreeReader
import java.io.StringReader
import edu.stanford.nlp.trees.Tree



class NLPParser {
    val maxLen:java.lang.Integer = 2;
	var parseProps = new Properties()
	parseProps.put("annotators", "tokenize, ssplit, pos, parse")
	parseProps.put("parser.maxlen", maxLen)
	var parseProcessor:StanfordCoreNLP = null;
	
	def init() {
	  parseProcessor = new StanfordCoreNLP(parseProps)
	  println("--Parser Created");
	}
	
	def parseProcess(sentence:NLPSentence): SentenceContainer = {

	  ////////////////////////////////////////////
	  // Testing
	  ////////////////////////////////////////////	
	  val openTree = "(TOP (S (S (NP (DT The) (NNP Pennsylvania) (NN official)) (VP (VBD was) (ADVP (RB just)) (VP (VBG talking) (PP (IN about) (NP (CD one) (NN area,)))))) (CC but) (S (NP (PRP he)) (VP (VBD summed) (PRT (RP up)) (NP (NP (DT a) (NN winter) (NN storm)) (SBAR (WHNP (WDT that)) (S (VP (VBD struck) (NP (NP (RB much)) (PP (IN of) (NP (DT the) (JJ eastern) (NNP United) (NNP States)))) (PP (IN on) (NP (NNP Wednesday.)))))))))))"
	  val s = new StringReader(openTree)
	  val ptr = new PennTreeReader(s);
	  val t = ptr.readTree()
	  
	  val stanfordTree = "(ROOT (S (S (NP (DT The) (NNP Pennsylvania) (NN official)) (VP (VBD was) (ADVP (RB just)) (VP (VBG talking) (PP (IN about) (NP (CD one) (NN area)))))) (, ,) (CC but) (S (NP (PRP he)) (VP (VBD summed) (PRT (RP up)) (NP (NP (DT a) (NN winter) (NN storm)) (SBAR (WHNP (WDT that)) (S (VP (VBD struck) (NP (NP (RB much)) (PP (IN of) (NP (DT the) (JJ eastern) (NNP United) (NNPS States)))) (PP (IN on) (NP (NNP Wednesday))))))))) (. .)))"
	  
	    
	  val tree:Tree = Tree.valueOf(t.toString());
	  val stree:Tree = Tree.valueOf(stanfordTree);
	  
	  ////////////////////////////////////////////
	  // Testing
	  ////////////////////////////////////////////		  
	  
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
		
//	    sentence.putTree("(ROOT (S (NP (PRP It)) (VP (VBZ 's) (NP (NP (NN kind)) (PP (IN of) (NP (NN fun))) (S (VP (TO to) (VP (VB do) (NP (DT the) (JJ impossible))))))) (. .)))")
//		SentenceContainer(sentence)
	}
}