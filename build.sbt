lazy val root = project
  .in(file("."))
  .settings(
    name := "playjpa-root",
    scalaVersion := "2.11.8",
    packagedArtifacts := Map.empty,
    publishLocal := {},
    publish := {}
  )
  .aggregate(core, plugin)

lazy val core = project
  .in(file("playjpa"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" % "play_2.11" % "2.5.3" intransitive,
      "com.typesafe.play" % "play-java-jpa_2.11" % "2.5.3" intransitive,
      "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final",
      "org.apache.commons" % "commons-lang3" % "3.4",
      "javax.inject" % "javax.inject" % "1"
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