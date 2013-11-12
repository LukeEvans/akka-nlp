name := "NLP Akka"

version := "1.0"

scalaVersion := "2.10.3"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-kernel_2.10" % "2.2.3"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.2.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")) 

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "0.20.5"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.3"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.2.3"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.0-alpha4"




