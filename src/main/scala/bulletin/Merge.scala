package bulletin

import scala.annotation.implicitNotFound
import shapeless._
import shapeless.labelled._

@implicitNotFound("Cannot update a ${A} with a ${B}. Check the field types match up, or manually create a Merge instance for these types.")
trait Merge[A, B] extends ((A, B) => A)

object Merge extends MergeFunctions with MergeImplicits

private[bulletin] trait MergeFunctions {
  /** Summon a [[Merge]] instance from implicit scope. */
  def apply[A, B](implicit merge: Merge[A, B]): Merge[A, B] =
    merge

  /** Create a [[Merge]] instance from a function. */
  def instance[A, B](func: (A, B) => A): Merge[A, B] =
    new Merge[A, B] {
      def apply(a: A, b: B): A = func(a, b)
    }
}

private[bulletin] trait MergeImplicits extends LowPriorityMergeImplicits with HighPriorityMergeImplicits

private[bulletin] trait LowPriorityMergeImplicits {
  implicit def requiredMerge[A, B](
    implicit ev: B <:< A
  ): Merge[A, B] =
    Merge.instance[A, B] { (original, update) =>
      update
    }

  implicit def optionalMerge[A]: Merge[A, Option[A]] =
    Merge.instance[A, Option[A]] { (original, update) =>
      update getOrElse original
    }
}

private[bulletin] trait HighPriorityMergeImplicits {
  /** Merge two non-empty HLists. */
  implicit def listMerge[Name <: Symbol, HeadA, TailA <: HList, HeadB, TailB <: HList](
    implicit
    witness: Witness.Aux[Name],
    headMerge: Merge[HeadA, HeadB],
    tailMerge: Lazy[Merge[TailA, TailB]]
  ): Merge[FieldType[Name, HeadA] :: TailA, FieldType[Name, HeadB] :: TailB] =
    Merge.instance[FieldType[Name, HeadA] :: TailA, FieldType[Name, HeadB] :: TailB] { (a, b) =>
      val head: FieldType[Name, HeadA] = field[Name](headMerge(a.head, b.head))
      val tail: TailA = tailMerge.value(a.tail, b.tail)
      head :: tail
    }

  /** Merge two non-empty HLists. */
  implicit def skipMerge[Name <: Symbol, HeadA, TailA <: HList, ListB <: HList](
    implicit
    witness: Witness.Aux[Name],
    tailMerge: Lazy[Merge[TailA, ListB]]
  ): Merge[FieldType[Name, HeadA] :: TailA, ListB] =
    Merge.instance[FieldType[Name, HeadA] :: TailA, ListB] { (a, b) =>
      val head: FieldType[Name, HeadA] = a.head
      val tail: TailA = tailMerge.value(a.tail, b)
      head :: tail
    }

  /** Merge two empty HLists. */
  implicit val nilMerge: Merge[HNil, HNil] =
    Merge.instance[HNil, HNil] { (a, b) =>
      HNil
    }

  /** Merge two generic ADTs. */
  implicit def genericMerge[A, HListA <: HList, B, HListB <: HList](
    implicit
    genA: LabelledGeneric.Aux[A, HListA],
    genB: LabelledGeneric.Aux[B, HListB],
    merge: Merge[HListA, HListB]
  ): Merge[A, B] =
    Merge.instance[A, B] { (a, b) =>
      genA.from(merge(genA.to(a), genB.to(b)))
    }
}
