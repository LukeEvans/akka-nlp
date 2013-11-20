package com.winston.utlities

import java.math.BigInteger
import java.security.MessageDigest
import java.util.HashMap
import scala.collection.JavaConversions._
import scala.concurrent.Future
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.StringEscapeUtils
import edu.stanford.nlp.trees.Tree
import java.util.ArrayList
import com.winston.nlp.NLPWord

object Tools {
  
  def getStringFromList(wordList:ArrayList[NLPWord]):String = {
    var lastWord:NLPWord = null
    var sentence:String = ""
    
    for(word <- wordList){
      if(word.remove){
    	  //do nothing
      }
      else if(lastWord == null || word.startIndex == lastWord.endIndex){
    	  sentence += word.value
      }
      else{
    	  sentence += (" " + word.value) 
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
  
  def cleanHtmlFormat(input:String):String = {
    var output = StringEscapeUtils.escapeHtml4(input);
    output = output.replaceAll("&rdquo;", "&quot;");
    output = output.replaceAll("&ldquo;", "&quot;");
    output = output.replaceAll("&lsquo;", "'");
    output = output.replaceAll("&rsquo;", "'");
    output = output.replaceAll("&mdash;", "-");
    output = StringEscapeUtils.unescapeHtml4(output);
 
    return output
  }
}