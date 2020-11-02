import sbt._

object Dependencies {
  val akka       = "2.6.8"
  val akkaHttp   = "10.2.0"
  val akkaKCL    = "3.0.1"
  val alpakkaES  = "2.0.2" // akka-stream-alpakka-elasticsearch
  val cats       = "2.1.1"
  val circe      = "0.12.3"
  val doobie     = "0.9.0"
  val logback    = "1.2.3"
  val macwire    = "2.3.7"
  val pureConfig = "0.14.0"
  val scalaTest  = "3.2.0"	

  val deps = Seq(
    "com.typesafe.akka"     %% "akka-slf4j"        % akka,
    "com.typesafe.akka"     %% "akka-actor-typed"  % akka,
    "com.typesafe.akka"     %% "akka-stream-typed" % akka,
    "ch.qos.logback"         % "logback-classic"   % logback,
    "com.lightbend.akka"    %% "akka-stream-alpakka-elasticsearch" % alpakkaES,
    "com.streetcontxt"      %% "kcl-akka-stream"                   % akkaKCL,
    "io.circe"              %% "circe-core"                        % circe,
    "io.circe"              %% "circe-generic"                     % circe,
    "io.circe"              %% "circe-parser"                      % circe,
    "com.github.pureconfig" %% "pureconfig"                        % pureConfig,
    "com.amazonaws"          % "aws-encryption-sdk-java"           % "1.6.2",
    "com.amazonaws"          % "aws-java-sdk-kms"                  % "1.11.889"
  )
}
