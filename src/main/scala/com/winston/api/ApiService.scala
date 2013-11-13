package com.winston.api

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

class ApiActor extends Actor with ApiService{
  
  def actorRefFactory = context
  
  def receive = {
    case a:Any => runRoute(myRoute)
  }
  
}

trait ApiService extends HttpService{
  
  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Reducto API</h1>
              </body>
            </html>
          }
        }
      }
    }
    val summarize =
    path("/health") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Status Ok</h1>
              </body>
            </html>
          }
        }
      }
    }
}