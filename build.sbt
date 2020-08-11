name := "SecureMedMail"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "org.squeryl" % "squeryl_2.11" % "0.9.6-RC3",
  "com.amazonaws" % "aws-java-sdk" % "1.6.5",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.apache.commons" % "commons-email" % "1.3.2"
)     

