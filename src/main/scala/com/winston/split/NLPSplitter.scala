package com.winston.split

import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import com.winston.nlp.transport.messages._
import com.winston.nlp.SentenceSet
import com.winston.nlp.NLPSentence
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import com.winston.nlp.transport.ReductoRequest
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import java.io.FileInputStream
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.tokenize.TokenizerME
import java.io.InputStream
import opennlp.tools.tokenize.Tokenizer

class NLPSplitter{
        var currentPath = System.getProperty("user.dir")
        var splitProps:Properties = new Properties();
        splitProps.put("annotators", "tokenize, ssplit");
        var splitProcessor:StanfordCoreNLP = null;
        var tokenizer:Tokenizer = null;
        var tokenModelIn:InputStream = new FileInputStream(currentPath + "/src/main/resources/en-token.bin");
        var tokenModel:TokenizerModel = null
        
        def init() {
                splitProcessor = new StanfordCoreNLP(splitProps)
                println("--Splitter Created");
                tokenModel = new TokenizerModel(tokenModelIn);
                tokenizer = new TokenizerME(tokenModel);
                tokenModelIn.close()
        }
        
        def splitProcess(request: ReductoRequest):SetContainer = {
          var document:Annotation = null
          var set:SentenceSet = null
                
          if(request.headline != null && request.text != null){
        	  set = new SentenceSet(request.headline, request.text);                
        	  document = new Annotation(request.text)
          }
          else return null
        		  
          splitProcessor.annotate(document)                        
          var list = document.get(classOf[SentencesAnnotation])
                                                
          for(m <- list){
        	  
        	  var sentence = new NLPSentence(m.get(classOf[TextAnnotation]));                                          
        	  val spans = tokenizer.tokenizePos(sentence.value)
        	  val tokens = tokenizer.tokenize(sentence.value)
                                                        
        	  for(idx <- 0 to spans.length-1){
        		  val span = spans(idx);
        		  sentence.addWord(tokens(idx), span.getStart(), span.getEnd(), tokens(idx))
        	  }
        	  set.addSentence(sentence);
          }                        
        SetContainer(set);    
       }
}