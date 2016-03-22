name         in ThisBuild := "bulletin"
organization in ThisBuild := "com.davegurnell"
version      in ThisBuild := "0.6.0"

// Compiler / depencencies

scalaVersion   := "2.11.7"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "com.chuusai"   %% "shapeless" % "2.2.5",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

// Bintray

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

bintrayPackageLabels in bintray := Seq("scala", "shapeless", "generic", "utility", "merge")
bintrayRepository    in bintray := "maven"
