package bulletin

import scala.annotation.implicitNotFound
import shapeless._

@implicitNotFound("Cannot merge a field of type ${A} with an update of type ${B}.")
trait Merge[A, B] {
  def apply(original: A, update: B): A
}

object Merge extends MergeFunctions with MergeInstances

trait MergeFunctions {
  /** Summon a [[Merge]] instance from implicit scope. */
  def apply[A, B](implicit merge: Merge[A, B]): Merge[A, B] =
    merge

  /** Create a [[Merge]] instance from a function. */
  def instance[A, B](func: (A, B) => A): Merge[A, B] =
    new Merge[A, B] { def apply(a: A, b: B): A = func(a, b) }
}

trait MergeInstances {
  implicit def overwriteMerge[A, B](implicit ev: B <:< A): Merge[A, B] =
    Merge.instance[A, B] { (original, update) =>
      update
    }

  implicit def optionalMerge[A, B](implicit merge: Merge[A, B]): Merge[A, Option[B]] =
    Merge.instance[A, Option[B]] { (original, update) =>
      update map (merge(original, _)) getOrElse original
    }
}
