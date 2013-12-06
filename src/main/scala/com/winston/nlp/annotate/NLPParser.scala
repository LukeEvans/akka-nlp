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
import java.io.InputStream
import java.io.FileInputStream
import opennlp.tools.parser.ParserModel
import opennlp.tools.parser.Parser
import opennlp.tools.parser.ParserFactory
import opennlp.tools.cmdline.parser.ParserTool
import opennlp.tools.parser.Parse
import opennlp.tools.parser.AbstractBottomUpParser
import opennlp.tools.util.Span
import opennlp.tools.tokenize.Tokenizer
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.tokenize.TokenizerME



class NLPParser {
  
	var currentPath = System.getProperty("user.dir")
	
	var parseProps = new Properties()
	parseProps.put("annotators", "tokenize, ssplit, pos, parse")
	var parseProcessor:StanfordCoreNLP = null;
	var parseModelIn:InputStream =  new FileInputStream(currentPath + "/src/main/resources/en-parser-chunking.bin")
	var tokenizer:Tokenizer = null;
	var tokenModelIn:InputStream = new FileInputStream(currentPath + "/src/main/resources/en-token.bin");
	var parseModel:ParserModel = null;
	var tokenModel:TokenizerModel = null
	var parser:Parser = null;
	
	def init() {
	  parseModel = new ParserModel(parseModelIn)
	  parser = ParserFactory.create(parseModel)
	  parseModelIn.close()
	  
	  tokenModel = new TokenizerModel(tokenModelIn);
	  tokenizer = new TokenizerME(tokenModel);
	  tokenModelIn.close()
	  
	  println("--Parser Created");
	}
	
	def parseProcess(sentence:NLPSentence): SentenceContainer = {

		var parse = new Parse(sentence.value, new Span(0, sentence.value.length), AbstractBottomUpParser.INC_NODE, 1, 0)
		
		val spans = tokenizer.tokenizePos(sentence.value)
		
//		for(idx <- 0 to spans.length-1){
//		  try {
//			val span = spans(idx);
//			// flesh out the parse with individual token sub-parses 
//			parse.insert(new Parse(sentence.value, span, AbstractBottomUpParser.TOK_NODE, 0, idx));
//		  }
//		}
 
		for(idx <- 0 to spans.length-1){
			val span = spans(idx);
//			scala.util.control.Exception.ignoring(classOf[Exception]) {
				// flesh out the parse with individual token sub-parses 
				parse.insert(new Parse(sentence.value, span, AbstractBottomUpParser.TOK_NODE, 0, idx));
//			}
		}
		
//		for(idx <- 0 to spans.length-1){
//			  try {
//				val span = spans(idx);
//				try {
//				// flesh out the parse with individual token sub-parses 
//				parse.insert(new Parse(sentence.value, span, AbstractBottomUpParser.TOK_NODE, 0, idx));
//				} catch {
//				case e:Exception => {}
//				}
//			  }
//		} 
		
		
		var actualParse = parser.parse(parse);
		
		val buffer = new StringBuffer();
		actualParse.show(buffer)
		val treeString = buffer.toString();
		sentence.putTree(treeString)
		
		SentenceContainer(sentence)
	}
}