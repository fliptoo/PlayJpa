lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.11.8",
    name := "playjpa-root",
    packagedArtifacts := Map.empty,
    publishLocal := {},
    publish := {}
  )
  .aggregate(core, plugin)

lazy val core = project
  .in(file("playjpa"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-java-jpa" % "2.5.4" % "provided",
      "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final",
      "org.apache.commons" % "commons-lang3" % "3.4"
    ),
    scalaVersion := "2.11.8",
    name := "playjpa",
    version := "1.0.3",
    organization := "com.fliptoo",
    autoScalaLibrary := false,
    crossPaths := false
  )

lazy val plugin = project
  .in(file("sbt"))
  .dependsOn(core)
  .settings(
    name := "sbt-playjpa",
    version := "1.0.3",
    organization := "com.fliptoo",
    sbtPlugin := true,
    autoScalaLibrary := false,
    crossPaths := false
  )
