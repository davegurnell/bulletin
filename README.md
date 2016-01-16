# Bulletin

A library for automatically performing shallow merges on case classes.
Bring your data the latest updates!

Copyright 2016 Dave Gurnell. Licensed [Apache 2][license].

[![Build Status](https://travis-ci.org/davegurnell/bulletin.svg?branch=develop)](https://travis-ci.org/davegurnell/bulletin)

## Getting Started

Grab the code from Bintray by adding the following to your `build.sbt`:

~~~ scala
scalaVersion := "2.11.7"

resolvers += "Awesome Utilities" at "https://dl.bintray.com/davegurnell/maven"

libraryDependencies += "io.underscore" %% "bulletin" % "0.1.0"
~~~

Create two case classes representing a data structure and an update:

~~~ scala
case class Person(
  id: Long,
  name: String,
  email: Option[String])

case class Update(
  id: Option[Long],
  name: Option[String],
  email: Option[Option[String]])
~~~

Create instances of those classes:

~~~ scala
val person = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
val update = Update(None, Some("Batman"), Some(None))
~~~

And merge them!

~~~ scala
val updated = person merge update
// updated: Person = Person(123L, "Batman", None)
~~~

Your update type has to have the same fields as your data type,
with each field wrapped in an `Option`.
Otherwise you'll get a compile error:

~~~ scala
person merge "This isn't an update!"
// compile error:
//   Cannot update a Person with a String.
//   Check the field types match up,
//   or manually create a Merge instance for these types.
~~~
