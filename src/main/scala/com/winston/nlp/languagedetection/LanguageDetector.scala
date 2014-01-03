package com.winston.nlp.languagedetection

import me.champeau.ld.UberLanguageDetector
import com.winston.nlp.transport.ReductoResponse
import com.winston.nlp.SummaryResult

class LanguageDetector {
  val detector = UberLanguageDetector.getInstance()
  
  def detectLanguage(text:String):ReductoResponse = {
    try{
      detector.detectLang(text) match{
        case "en" => null
        case other:Any => 
          return new ReductoResponse(new SummaryResult()).setStatus("OK - Unsupported Language: "+mapIdToLanguage(other))
      }
    } catch{
      case e:Exception =>
        e.printStackTrace()
        return new ReductoResponse(new SummaryResult())
    }
  }
  
  def mapIdToLanguage(id:String):String = {
    id match{
      case "af" => "Afrikaans"
      case "ar" => "Arabic"
      case "bg" => "Bulgarian"
      case "bn" => "Bengali"
      case "cs" => "Czech"
      case "da" => "Danish"
      case "de" => "German"
      case "el" => "Greek"
      case "es" => "Spanish"
      case "et" => "Estonian"
      case "fa" => "Persian"
      case "fi" => "Finnish"
      case "fr" => "French"
      case "hi" => "Hindi"
      case "id" => "Indonesian"
      case "he" => "Hebrew"
      case "it" => "Italian"
      case "ja" => "Japanese"
      case "ko" => "Korean"
      case "ne" => "Nepali"
      case "no" => "Norwegian"
      case "pt" => "Portuguese"
      case "ru" => "Russian"
      case "sv" => "Swedish"
      case "vi" => "Vietnamese"
      case "zh-cn" => "Arabic"
      case "zh-tw" => "Chinese"
      case _:Any => "Unidentified Language"
    }
  }
}