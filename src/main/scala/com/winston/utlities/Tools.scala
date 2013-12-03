package com.winston.utlities

import java.math.BigInteger
import java.security.MessageDigest
import java.util.HashMap
import java.net.URL;
import java.net.URI;
import java.io.InputStreamReader
import scala.collection.JavaConversions._
import scala.concurrent.Future
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import edu.stanford.nlp.trees.Tree
import com.winston.nlp.NLPWord
import java.util.ArrayList
import java.io.BufferedReader
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpResponse

object Tools {
  
  def getStringFromList(wordList:ArrayList[NLPWord]):String = {
           var lastWord:NLPWord = null
           var sentence:String = ""
    
           for(word <- wordList){
                   if(!leafIsParenthetical(word.value)){
                     if(lastWord == null || word.startIndex == lastWord.endIndex)
                       sentence = sentence + word.value
                     else
                           sentence = sentence + (" "+ word.originalText)
                   }
                   else{
                     if(lastWord == null || word.startIndex == lastWord.endIndex)
                       sentence = sentence + word.value
                     else
                           sentence = sentence + (" "+ word.originalText)
                   }
                   lastWord = word
           }    
          return sentence
  }
  
  def getStringFromTree(tree:Tree):String = {
    var sb = new StringBuilder()
    
    var previous:String = null
    
    val tres:java.util.List[Tree] = tree.getLeaves()
    
    for(t <- tres){
      val value = t.value()
      if(leafIsWord(value) || leafIsLeadingSymbol(value)){
        if(!leafIsLeadingSymbol(previous)){
          sb.append(" ").append(t.toString())
        }
        else{
          sb.append(t.toString())
        }
      }
      else{
        sb.append(t.toString())
      }
      previous = value
    }
    
    return sb.toString
  }
  
  def leafIsWord(value:String):Boolean = value match{
    case null => false
    case value => {
      if(value.contains("'")){
        return false
      }
      val testValue = value.replaceAll("\\W", "")
      testValue.length() match{
        case 0 => false
        case _ => true
      }
    }
  }
  
  def leafIsLeadingSymbol(value:String):Boolean = value match {
  	case "$" => true
    case "``" => true
    case _ => false
  }
  
  def leafIsParenthetical(value:String):Boolean = {
    if(value.contains("RRB") || value.contains("LRB")) true else false
  }
  
  def generateRandomNumber():Int = {
    var Min = 0
    var Max = 65535
    
    var random:Int = Min + (Math.random() * ((Max - Min)+1)).asInstanceOf[Int]
    
    random -= 32768
    
    return random
  }
  
  def nodeFromMap(map:HashMap[String, Object]):ObjectNode = {
    try{
      
      var mapper = new ObjectMapper()
      var node = mapper.valueToTree(map)
      
      return node
      
    } catch{
      case e:Exception =>{
        e.printStackTrace()
        return null
      }
    }
  }
  
  def addObjectToJson(field:String, obj:Object, json:JsonNode):ObjectNode = {
    try{
      
      var mapper = new ObjectMapper()
      var node = json.asInstanceOf[ObjectNode]
      
      var jsonNode:JsonNode = mapper.valueToTree(obj)
      node.put(field, jsonNode)
      
      return node
      
    } catch{
      case e:Exception => {
        e.printStackTrace()
        return null
      }
    }
  }
  
  def jsonFromString(input:String):ObjectNode = {
    if(input != null && input.length() > 0){
      var objectMapper = new ObjectMapper()
      try{
        return objectMapper.readValue(input, classOf[ObjectNode])
      }catch{
        case e:Exception =>{
          e.printStackTrace()
          return null
        }
      }
    }
    else{
      return null
    }
  }
  
  def mergeBodyAndURL(params:java.util.Map[String, String], input:String):JsonNode = {
    var tempInput = escapeInput(input)
    
    var node:ObjectNode = jsonFromString(tempInput)
    
    if(node == null){
      var mapper = new ObjectMapper()
      node = mapper.createObjectNode()
    }
    
    
    for(key <- params.keySet()){
      node.put(key, params.get(key))
    }
    
    return node
  }
  
  def escapeInput(input:String):String = {
    return input.replace("\n", "").replace("\r", "")
  }
  
  def generateHash(s:String):String = {
    return md5(s.toLowerCase())
  }
  
  def md5(input:String):String = {
    var md5:String = null
    
    if(null == input) return null
    
    try{
      var digest = MessageDigest.getInstance("MD5")
      
      digest.update(input.getBytes(), 0, input.length())
      
      md5 = new BigInteger(1, digest.digest()).toString(16)
      
    } catch{
      case e:Exception => {
        e.printStackTrace()
        return null
      }
    }
    return md5
  }
  
  def fetchURL(url:String):JsonNode = {
	try {
		var httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter("http.socket.timeout", new Integer(20000));
			var getRequest = new HttpGet(parseUrl(url).toString());
			getRequest.addHeader("accept", "application/json");

			var response = httpClient.execute(getRequest);

			// Return JSON
			var mapper = new ObjectMapper();
			var reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			return mapper.readTree(reader);

		} catch{
		  case e:Exception =>{
			System.out.println("Failure: " + url);
			e.printStackTrace();
			return null;
		  }
		}
	}
  
  	//================================================================================
	// URL encoding Methods
	//================================================================================
	def parseUrl(s:String):URL = {
		var u:URL = null;
		try {
			u = new URL(s);
			try {
				return new URI(
						u.getProtocol(), 
						u.getAuthority(), 
						u.getPath(),
						u.getQuery(), 
						u.getRef()).toURL();
			} catch {		  
			  	case e:Exception=> {
			  		e.printStackTrace();
			
			  	}
			}
		} 
		return null;
	}
}