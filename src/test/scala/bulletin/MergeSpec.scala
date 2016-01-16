package bulletin

import org.scalatest._
import shapeless.test.illTyped

class MergeSpec extends FreeSpec with Matchers {
  case class Person(id: Long, name: String, email: Option[String])

  case class Update(id: Option[Long], name: Option[String], email: Option[Option[String]])

  "updates are applied correctly" in {
    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = Update(Some(321L), Some("Batman"), Some(Some("iam@thebatman.com")))

    val actual   = person merge update
    val expected = Person(321L, "Batman", Some("iam@thebatman.com"))

    actual should equal(expected)
  }

  "noop updates cause no changes" in {
    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = Update(None, None, None)

    val actual   = person merge update
    val expected = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))

    actual should equal(expected)
  }

  "optional fields can be set to none" in {
    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = Update(Some(321L), Some("Batman"), Some(None))

    val actual   = person merge update
    val expected = Person(321L, "Batman", None)

    actual should equal(expected)
  }

  "incompatible datatypes cause a compile error" in {
    case class Update2(id: Option[Int], name: Option[String], email: Option[Option[String]])
    case class Update3(name: Option[String], email: Option[Option[String]])

    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update2  = Update2(Some(321), Some("Batman"), Some(None))
    val update3  = Update3(Some("Batman"), Some(None))

    illTyped("""person merge update2""")
    illTyped("""person merge update3""")
  }
}