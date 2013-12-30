package com.winston.urlextraction

import com.winston.utlities.Tools
import com.fasterxml.jackson.databind.JsonNode
import akka.actor.ActorSystem

class URLExtractor() {
        val diffbotBase:String = "http://www.diffbot.com/api/article?token=2a418fe6ffbba74cd24d03a0b2825ea5&url="
        
        def getExtraction(url:String):(String, String) ={
          var response:JsonNode = Tools.fetchURL(diffbotBase+url)
          if(response.get("text").isMissingNode() || response.get("title").isMissingNode()){
            return null
          }
          else{
            return (response.get("title").asText(), lintBody(response.get("text").asText()))
          }
        }
        
        def lintBody(text:String):String = {
          var paragraphs = text.split("[\\r\\n]")
          
          var newParagraphs = ""
            
          for(paragraph <- paragraphs){
            var testString = paragraph.replaceAll("\\W", "NONWORD")
            
            if(testString.endsWith("NONWORD")){
                    if(paragraph.endsWith(":") || paragraph.endsWith(")")) {
                      if(paragraph.length() >= 85){
                             System.out.println("plen : " + paragraph.length())
                             newParagraphs += paragraph + "\n"
                      }
                    }
                    else{
                      newParagraphs += paragraph + "\n"
                    }
            }  
          }
          return newParagraphs
        }
}