package com.winston.nlp.parse
import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties
import edu.stanford.nlp.ling.CoreAnnotations._
import com.winston.nlp.NLPSentence
import com.winston.nlp.transport.messages._
import java.io.InputStream
import java.io.FileInputStream
import opennlp.tools.parser.ParserModel
import opennlp.tools.parser.Parser
import opennlp.tools.parser.ParserFactory
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
	
	var parseModelIn:InputStream = null;
	
	try {
		parseModelIn =  new FileInputStream("/usr/local/reducto-dist" + "/config/en-parser-chunking.bin")
	} catch {
	  	case e: Exception => println("\n\nError opening file\n\n"); e.printStackTrace();
//	  	case e: Exception => parseModelIn =  new FileInputStream(currentPath + "/src/main/resources/en-parser-chunking.bin")
	}
	
	var tokenModelIn:InputStream = null;
	
	try { 
		tokenModelIn = new FileInputStream("/usr/local/reducto-dist" + "/config/en-token.bin");
	} catch {
	    case e: Exception => println("\n\nError opening file\n\n"); e.printStackTrace();
//	  	case e: Exception => tokenModelIn = new FileInputStream(currentPath + "/src/main/resources/en-token.bin");
	}
	
	var tokenizer:Tokenizer = null;
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
		
		for(idx <- 0 to spans.length-1){
			val span = spans(idx);
			// flesh out the parse with individual token sub-parses 
			parse.insert(new Parse(sentence.value, span, AbstractBottomUpParser.TOK_NODE, 0, idx));
		}
		
		var actualParse = parser.parse(parse);
		
		val buffer = new StringBuffer();
		actualParse.show(buffer)
		val treeString = buffer.toString();
		sentence.putTree(treeString)
		
		SentenceContainer(sentence)
	  
//	  val newSentence = sentence.copy;
//	  newSentence.putTree("(ROOT (S (NP (PRP It)) (VP (VBZ 's) (NP (NP (NN kind)) (PP (IN of) (NP (NN fun))) (S (VP (TO to) (VP (VB do) (NP (DT the) (JJ impossible))))))) (. .)))")
//	  SentenceContainer(newSentence)
	}
}