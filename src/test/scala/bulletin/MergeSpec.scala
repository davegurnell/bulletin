package bulletin

import org.scalatest._
import shapeless.test.illTyped

class MergeSpec extends FreeSpec with Matchers {
  case class Person(id: Long, name: String, email: Option[String])
  case class RequiredUpdate(id: Long, name: Option[String], email: Option[String])
  case class OptionalUpdate(id: Option[Long], name: Option[String], email: Option[Option[String]])
  case class CustomUpdate(id: Double)

  "updates are applied correctly" in {
    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = OptionalUpdate(Some(321L), Some("Batman"), Some(Some("iam@thebatman.com")))

    val actual   = person merge update
    val expected = Person(321L, "Batman", Some("iam@thebatman.com"))

    actual should equal(expected)
  }

  "noop updates cause no changes" in {
    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = OptionalUpdate(None, None, None)

    val actual   = person merge update
    val expected = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))

    actual should equal(expected)
  }

  "required fields are permitted" in {
    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = RequiredUpdate(321L, Some("Batman"), None)

    val actual   = person merge update
    val expected = Person(321L, "Batman", None)

    actual should equal(expected)
  }

  "optional fields can be set to none" in {
    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = OptionalUpdate(Some(321L), Some("Batman"), Some(None))

    val actual   = person merge update
    val expected = Person(321L, "Batman", None)

    actual should equal(expected)
  }

  "fields are aligned by type" in {
    case class InvalidUpdate(name: Option[String], email: Option[Option[String]])

    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = InvalidUpdate(Some("Batman"), Some(None))

    val actual   = person merge update
    val expected = Person(123L, "Batman", None)

    actual should equal(expected)
  }

  "incompatible datatypes cause a compile error" in {
    case class InvalidUpdate(id: Option[Int], name: Option[String], email: Option[Option[String]])

    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update2  = InvalidUpdate(Some(321), Some("Batman"), Some(None))

    illTyped("""person merge update2""")
  }

  "custom merge instances can be used" in {
    implicit val doubleIntMerge: Merge[Long, Double] =
      Merge.instance[Long, Double] { (original, update) =>
        (update * 1000).toInt
      }

    val person   = Person(123L, "Bruce Wayne", Some("bruce@waynemanor.com"))
    val update   = CustomUpdate(0.321)

    val actual   = person merge update
    val expected = Person(321L, "Bruce Wayne", Some("bruce@waynemanor.com"))

    actual should equal(expected)
  }

}
