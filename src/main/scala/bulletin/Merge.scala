package bulletin

import scala.annotation.implicitNotFound
import shapeless._
import shapeless.labelled._

@implicitNotFound("Cannot update a ${A} with a ${B}. Check the field types match up, or manually create a Merge instance for these types.")
trait Merge[A, B] extends ((A, B) => A)

object Merge extends MergeConstructors
  with MergeImplicits

private[bulletin] trait MergeConstructors {
  /** Summon a [[Merge]] instance from implicit scope. */
  def apply[A, B](implicit merge: Merge[A, B]): Merge[A, B] =
    merge

  /** Create a [[Merge]] instance from a function. */
  def create[A, B](func: (A, B) => A): Merge[A, B] =
    new Merge[A, B] {
      def apply(a: A, b: B): A = func(a, b)
    }
}

private[bulletin] trait MergeImplicits {
  self: MergeConstructors =>

  /** Merge two non-empty HLists. */
  implicit def listMerge[HeadA, TailA <: HList, HeadB, TailB <: HList](
    implicit
    evidence: HeadB <:< Option[HeadA],
    tailMerge: Lazy[Merge[TailA, TailB]]
  ): Merge[HeadA :: TailA, HeadB :: TailB] =
    create[HeadA :: TailA, HeadB :: TailB] { (a, b) =>
      val head: HeadA = b.head getOrElse a.head
      val tail: TailA = tailMerge.value(a.tail, b.tail)
      head :: tail
    }

  /** Merge two non-empty HLists. */
  implicit def skipMerge[HeadA, TailA <: HList, ListB <: HList](
    implicit
    tailMerge: Lazy[Merge[TailA, ListB]]
  ): Merge[HeadA :: TailA, ListB] =
    create[HeadA :: TailA, ListB] { (a, b) =>
      val head: HeadA = a.head
      val tail: TailA = tailMerge.value(a.tail, b)
      head :: tail
    }

  /** Merge two empty HLists. */
  implicit val nilMerge: Merge[HNil, HNil] =
    create[HNil, HNil] { (a, b) =>
      HNil
    }

  /** Merge two generic ADTs. */
  implicit def genericMerge[A, HListA <: HList, B, HListB <: HList](
    implicit
    genA: Generic.Aux[A, HListA],
    genB: Generic.Aux[B, HListB],
    merge: Merge[HListA, HListB]
  ): Merge[A, B] =
    create[A, B] { (a, b) =>
      genA.from(merge(genA.to(a), genB.to(b)))
    }
}
