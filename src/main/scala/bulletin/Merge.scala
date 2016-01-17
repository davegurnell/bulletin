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
  implicit def listMerge[Name <: Symbol, HeadA, TailA <: HList, HeadB, TailB <: HList](
    implicit
    witness: Witness.Aux[Name],
    evidence: HeadB <:< Option[HeadA],
    tailMerge: Merge[TailA, TailB]
  ): Merge[FieldType[Name, HeadA] :: TailA, FieldType[Name, HeadB] :: TailB] =
    create[FieldType[Name, HeadA] :: TailA, FieldType[Name, HeadB] :: TailB] { (a, b) =>
      val head: FieldType[Name, HeadA] = field[Name](b.head getOrElse a.head)
      val tail: TailA = tailMerge(a.tail, b.tail)
      head :: tail
    }

  /** Merge two non-empty HLists. */
  implicit def skipMerge[Name <: Symbol, HeadA, TailA <: HList, ListB <: HList](
    implicit
    witness: Witness.Aux[Name],
    tailMerge: Merge[TailA, ListB]
  ): Merge[FieldType[Name, HeadA] :: TailA, ListB] =
    create[FieldType[Name, HeadA] :: TailA, ListB] { (a, b) =>
      val head: FieldType[Name, HeadA] = a.head
      val tail: TailA = tailMerge(a.tail, b)
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
    genA: LabelledGeneric.Aux[A, HListA],
    genB: LabelledGeneric.Aux[B, HListB],
    merge: Merge[HListA, HListB]
  ): Merge[A, B] =
    create[A, B] { (a, b) =>
      genA.from(merge(genA.to(a), genB.to(b)))
    }
}
