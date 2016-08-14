name         in ThisBuild := "bulletin"
organization in ThisBuild := "com.davegurnell"
version      in ThisBuild := "0.6.0"
scalaVersion in ThisBuild := "2.11.8"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "com.chuusai"   %% "shapeless" % "2.2.5",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

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
