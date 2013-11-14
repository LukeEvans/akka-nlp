name := "NLP Akka"

version := "1.0"

scalaVersion := "2.10.3"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

atmosSettings

traceAkka("2.2.3")

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-kernel_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-cluster_2.10" % "2.2.3"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.2.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")) 

libraryDependencies += "com.sksamuel.elastic4s" % "elastic4s_2.10" % "0.90.5.2"
