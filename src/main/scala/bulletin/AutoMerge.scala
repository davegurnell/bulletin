package bulletin

import scala.annotation.implicitNotFound
import shapeless._
import shapeless.labelled._

@implicitNotFound("Cannot generate an AutoMerge instance for ${A} and ${B}. Check the field types match up, and manually create Merge instances for any non-standard pairs of types.")
sealed trait AutoMerge[A, B] {
  def apply(original: A, update: B): A
}

object AutoMerge extends AutoMergeFunctions with AutoMergeInstances

trait AutoMergeFunctions {
  /** Summon an [[AutoMerge]] instance from implicit scope. */
  def apply[A, B](implicit merge: AutoMerge[A, B]): AutoMerge[A, B] =
    merge

  /** Create an [[AutoMerge]] instance from a function. */
  def instance[A, B](func: (A, B) => A): AutoMerge[A, B] =
    new AutoMerge[A, B] { def apply(a: A, b: B): A = func(a, b) }
}

trait AutoMergeInstances {
  implicit def headAutoMerge[Name <: Symbol, HeadA, TailA <: HList, HeadB, TailB <: HList](
    implicit
    witness: Witness.Aux[Name],
    headMerge: Merge[HeadA, HeadB],
    tailMerge: Lazy[AutoMerge[TailA, TailB]]
  ): AutoMerge[FieldType[Name, HeadA] :: TailA, FieldType[Name, HeadB] :: TailB] =
    AutoMerge.instance[FieldType[Name, HeadA] :: TailA, FieldType[Name, HeadB] :: TailB] { (a, b) =>
      val head: FieldType[Name, HeadA] = field[Name](headMerge(a.head, b.head))
      val tail: TailA = tailMerge.value(a.tail, b.tail)
      head :: tail
    }

  /** Merge two non-empty HLists. */
  implicit def skipAutoMerge[Name <: Symbol, HeadA, TailA <: HList, ListB <: HList](
    implicit
    witness: Witness.Aux[Name],
    tailMerge: Lazy[AutoMerge[TailA, ListB]]
  ): AutoMerge[FieldType[Name, HeadA] :: TailA, ListB] =
    AutoMerge.instance[FieldType[Name, HeadA] :: TailA, ListB] { (a, b) =>
      val head: FieldType[Name, HeadA] = a.head
      val tail: TailA = tailMerge.value(a.tail, b)
      head :: tail
    }

  /** Merge two empty HLists. */
  implicit val nilAutoMerge: AutoMerge[HNil, HNil] =
    AutoMerge.instance[HNil, HNil] { (a, b) =>
      HNil
    }

  /** Merge two generic ADTs. */
  implicit def genericAutoMerge[A, HListA <: HList, B, HListB <: HList](
    implicit
    genA: LabelledGeneric.Aux[A, HListA],
    genB: LabelledGeneric.Aux[B, HListB],
    merge: AutoMerge[HListA, HListB]
  ): AutoMerge[A, B] =
    AutoMerge.instance[A, B] { (a, b) =>
      genA.from(merge(genA.to(a), genB.to(b)))
    }
}
