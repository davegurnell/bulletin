# Bulletin

A library for automatically performing shallow merges on case classes.
Treat your data structures to the latest updates!

Copyright 2016 Dave Gurnell. Licensed [Apache 2][license].

[![Build Status](https://travis-ci.org/davegurnell/bulletin.svg?branch=develop)](https://travis-ci.org/davegurnell/bulletin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.davegurnell/bulletin_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.davegurnell/bulletin_2.11)

## Getting Started

Grab the code by adding the following to your `build.sbt`:

~~~ scala
libraryDependencies += "com.davegurnell" %% "bulletin" % "<<VERSION>>"
~~~

Create two case classes representing a data structure and an update:

~~~ scala
case class Person(
  id: Long,
  name: String,
  email: Option[String])

case class Update(
  name: Option[String],
  email: Option[Option[String]])
~~~

Create instances of those classes:

~~~ scala
val person = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
val update = Update(Some("Batman"), Some(None))
~~~

And merge them!

~~~ scala
import bulletin._

val updated = person merge update
// updated: Person = Person(123L, "Batman", None)
~~~

No update function is required.

Bulletin matches fields in `Update` to fields in `Person` by name and type.
The only restriction is that the fields have to be defined in the same order.

If a field in the update has the same type as a field in the original,
it always overwrites the original.

If a field in the update is an `Option` of a field in the original,
it overwrites the original iff it is set to `Some`.

If you try to merge a data structure that doesn't conform to the correct patterns,
you'll get a compile error:

~~~ scala
person merge "This isn't an update!"
// compile error:
//   Cannot update a Person with a String.
//   Check the field types match up,
//   or manually create a Merge instance for these types.
~~~

[license]: http://www.apache.org/licenses/LICENSE-2.0
