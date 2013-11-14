package com.reactor.nlp.config

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

object SystemCreator {

    //================================================================================
	// Create Base system
	//================================================================================
    def createBaseSystem(name: String, hostName: String, port: String) : ActorSystem = {

    		val string: Config = ConfigFactory.parseString(
    				s"""akka {
                        	actor {
                            	provider = "akka.remote.RemoteActorRefProvider"
    				
    				            serializers {
                                	java = "akka.serialization.JavaSerializer"
                            	}
                                	
                            	serialization-bindings {
                                	"java.lang.String" = java
                                	"com.winston.nlp.messages.TransportMessage" = java
                            	}
                        	}
                        
    						remote {
                            	netty.tcp {
                                	hostname = "$hostName"
                                	port = $port
                                }
                            }
                                	
                     }
                      atmos {
                          trace {
                              enabled: true
                              node: Test
                              traceable {
                                "*": true
                              }
                              sampling {
                                "*": 1
                              }
                          }
                       }          	
                                	
                      """
    		)
        
    		return ActorSystem.create(name, ConfigFactory.load(string))
    }
    
    //================================================================================
	// Determine which client system we should create
	//================================================================================
    def createClientSystem(name: String, hostName: String, port: String) : ActorSystem = {
    		if (hostName.startsWith("127")) {
    		  return createLocalClientSystem(name, hostName, port)
    		}
    		
    		else {
    		  return createRemoteClientSystem(name, hostName, port)
    		}
    }
    
    //================================================================================
	// Create local client system
	//================================================================================
    private def createLocalClientSystem(name: String, hostName: String, port: String) : ActorSystem = {
       val string: Config = ConfigFactory.parseString(
           s""" 
             akka {
             	actor {
                	deployment {
        
           				/splitWorkers { 
           					router = "round-robin" 
                			nr-of-instances = 1
           					target { 
              					nodes = ["akka.tcp://DaemonSystem@127.0.0.1:2552"]
           					} 
           				}
        
           				/nlpWorkers { 
           					router = "round-robin" 
           					nr-of-instances = 1
           					target { 
           						nodes = ["akka.tcp://DaemonSystem@127.0.0.1:2552"] 
           					} 
           				}
        
           				/parseWorkers { 
           					router = "round-robin" 
           					nr-of-instances = 1
           					target { 
              					nodes = ["akka.tcp://DaemonSystem@127.0.0.1:2552"] 
           					} 
           				}
<<<<<<< HEAD
           
           				/comboWorkers {
           					router = "round-robin"
           					nr-of-instances = 1
           					target {
           						nodes = ["akka.tcp://DaemonSystem@127.0.0.1:2552"]
           					}
           				}
=======
           			}
                                 
           			serializers {
                    	java = "akka.serialization.JavaSerializer"
                    }
                                	
                    serialization-bindings {
                        "java.lang.String" = java
                        "com.winston.nlp.messages.TransportMessage" = java
                    }
>>>>>>> ClusterMark1
           		}
           
            actor.provider = "akka.remote.RemoteActorRefProvider"
           	remote.netty.tcp.port = $port
           	remote.netty.tcp.hostname = "$hostName"
           }
           """
           )
           
           return ActorSystem.create(name, ConfigFactory.load(string))
     }
     
    //================================================================================
	// Create remote client system
	//================================================================================
    private def createRemoteClientSystem(name: String, hostName: String, port: String) : ActorSystem = {
       val string: Config = ConfigFactory.parseString(
           s""" 
             akka {
             	actor {
                	deployment {
        
           				/splitWorkers { 
           					router = "round-robin" 
                			nr-of-instances = 1
           					target { 
              					nodes = ["akka.tcp://DaemonSystem@10.147.157.30:2552", "akka.tcp://DaemonSystem@10.179.1.7:2552", "akka.tcp://DaemonSystem@10.145.140.70:2552"] 
           					} 
           				}
        
           				/nlpWorkers { 
           					router = "round-robin" 
           					nr-of-instances = 1
           					target { 
           						nodes = ["akka.tcp://DaemonSystem@127.0.0.1:2552"] 
           					} 
           				}
        
           				/parseWorkers { 
           					router = "round-robin" 
           					nr-of-instances = 1
           					target { 
              					nodes = ["akka.tcp://DaemonSystem@127.0.0.1:2552"] 
           					} 
           				}
           		}
           }

            actor.provider = "akka.remote.RemoteActorRefProvider"
           	remote.netty.tcp.port = $port
           	remote.netty.tcp.hostname = "hostName"
           }
           """
           )
           
           return ActorSystem.create(name, ConfigFactory.load(string))
     }
}