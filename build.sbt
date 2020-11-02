import Dependencies._

lazy val settings = Seq(
  name := "aurora-activity-stream-consumer",
  organization := "higherkingpud",
  version := "0.1",
  scalaVersion := "2.13.3",
    scalacOptions := Seq(
      "-deprecation", // @deprecated なAPIが使われている箇所を警告します
      "-feature",     // langauge feature の import が必要な箇所を警告します
      "-unchecked",
      "-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused",
      "-Ywarn-value-discard"
    ),
  resolvers in ThisBuild += Resolver.bintrayRepo("streetcontxt", "maven"),
  libraryDependencies ++= deps
)

lazy val root = (project in file("."))
  .settings(settings)

