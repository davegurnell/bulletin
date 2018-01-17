name         := "bulletin"
organization := "com.davegurnell"
version      := "0.8.0"

scalaOrganization  in ThisBuild := "org.typelevel"
scalaVersion       in ThisBuild := "2.11.8"
crossScalaVersions in ThisBuild := Seq("2.11.8", "2.12.1")

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation"
)

libraryDependencies ++= Seq(
  "com.chuusai"   %% "shapeless" % "2.3.2",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

publishTo := sonatypePublishTo.value

pomExtra in Global := {
  <url>https://github.com/davegurnell/bulletin</url>
  <scm>
    <connection>scm:git:github.com/davegurnell/bulletin</connection>
    <developerConnection>scm:git:git@github.com:davegurnell/bulletin</developerConnection>
    <url>github.com/davegurnell/bulletin</url>
  </scm>
  <developers>
    <developer>
      <id>davegurnell</id>
      <name>Dave Gurnell</name>
      <url>http://davegurnell.com</url>
      <organization>Underscore</organization>
      <organizationUrl>http://underscore.io</organizationUrl>
    </developer>
  </developers>
}
