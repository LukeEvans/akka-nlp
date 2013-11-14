name := "NLP Akka"

version := "0.1"

scalaVersion := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

<<<<<<< HEAD
resolvers += "spray repo" at "http://repo.spray.io/"


//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.3"
=======
atmosSettings

traceAkka("2.2.3")

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.3"
>>>>>>> ClusterMark1

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-kernel_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-cluster_2.10" % "2.2.3"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.2.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")) 

<<<<<<< HEAD
libraryDependencies += "org.elasticsearch" % "elasticsearch" % "0.20.5"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.3"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.2.3"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.0-alpha4"

libraryDependencies ++= {
  val akkaV = "2.2.3"
  val sprayV = "1.2-RC2"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV,
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test"
  )
}

seq(Revolver.settings: _*)




=======
libraryDependencies += "com.sksamuel.elastic4s" % "elastic4s_2.10" % "0.90.5.2"
>>>>>>> ClusterMark1
