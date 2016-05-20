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
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= Seq(
      javaJpa,
      "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final",
      "org.apache.commons" % "commons-lang3" % "3.4",
      "javax.inject" % "javax.inject" % "1"
    ),
    scalaVersion := "2.11.8",
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
    sbtPlugin := true,
    autoScalaLibrary := false,
    crossPaths := false
  )
