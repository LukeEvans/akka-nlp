package com.winston.utlities

import scala.collection.JavaConversions._
import edu.stanford.nlp.trees.Tree
import javax.swing.ImageIcon
import java.awt.Image

object Tools {
	
//  def getImageFromURL(url:String){
//    var image:Image = new ImageIcon(parseUrl(url)).getImage()
//    
//    return image
//  }
  
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
  
}