lazy val root = project
  .in(file("."))
  .aggregate(core, plugin)
  .settings(
    name := "playjpa-root",
    scalaVersion := "2.11.8"
  )

lazy val core = project
  .in(file("playjpa"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" % "play_2.11" % "2.5.3",
      "com.typesafe.play" % "play-java-jpa_2.11" % "2.5.3",
      "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final"
    ),
    name := "playjpa",
    version := "1.0.0",
    organization := "com.fliptoo",
    autoScalaLibrary := false,
    crossPaths := false
  )

lazy val plugin = project
  .in(file("sbt"))
  .dependsOn(core)
  .settings(
    name := "sbt-playjpa",
    version := "1.0.0",
    organization := "com.fliptoo",
    sbtPlugin := true
  )