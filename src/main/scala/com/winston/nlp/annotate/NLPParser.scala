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
	var parseProps = new Properties()
	parseProps.put("annotators", "tokenize, ssplit, pos, parse")
	var parseProcessor:StanfordCoreNLP = null;
	var parseModelIn:InputStream =  new FileInputStream("/Users/kevincolin/Development/Winston/projects/akka-nlp/src/main/resources/en-parser-chunking.bin")	
	var tokenizer:Tokenizer = null;
	var tokenModelIn:InputStream = new FileInputStream("/Users/kevincolin/Development/Winston/projects/akka-nlp/src/main/resources/en-token.bin");
	var parseModel:ParserModel = null;
	var tokenModel:TokenizerModel = null
	var parser:Parser = null;
	
	def init() {
	  parseModel = new ParserModel(parseModelIn)
	  parser = ParserFactory.create(parseModel)
	  parseProcessor = new StanfordCoreNLP(parseProps)
	  parseModelIn.close()
	  
	  tokenModel = new TokenizerModel(tokenModelIn);
	  tokenizer = new TokenizerME(tokenModel);
	  tokenModelIn.close()
	  
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
		
		//var parse = ParserTool.parseLine(sentence.value, parser, 1)
		
		var parse = new Parse(sentence.value,
			new Span(0, sentence.value.length),
			AbstractBottomUpParser.INC_NODE,
			1,
			0)
		
		val spans = tokenizer.tokenizePos(sentence.value)
		
		if (trees.size() > 0) {
			println("Stanford tree: " + trees.get(0))
		} 
		for(idx <- 0 to spans.length-1){
			val span = spans(idx);
			// flesh out the parse with individual token sub-parses 
			parse.insert(new Parse(sentence.value,
            span,
            AbstractBottomUpParser.TOK_NODE, 
            0,
            idx));
		}
		
 
		var actualParse = parser.parse(parse);
		
		print("opennlp:       ")
		actualParse.show()
		
		SentenceContainer(sentence)
	}
}