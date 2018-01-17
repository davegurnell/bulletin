package bulletin

import scala.annotation.implicitNotFound
import shapeless._, ops.record.Remover
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

trait LowPriorityAutoMergeInstances {
  implicit def skipAutoMerge[HA, TA <: HList, ListB <: HList](implicit tailAutoMerge: AutoMerge[TA, ListB]) =
    AutoMerge.instance[HA :: TA, ListB]{(a, b) =>
      a.head :: tailAutoMerge(a.tail, b)
    }
}

trait AutoMergeInstances extends LowPriorityAutoMergeInstances {
  implicit def headAutoMerge[Name <: Symbol, HeadA, TailA <: HList, HeadB, TailB <: HList, ListB <: HList](
    implicit
    witness: Witness.Aux[Name],
    select: Remover.Aux[ListB, Name, (HeadB, TailB)],
    headMerge: Merge[HeadA, HeadB],
    tailMerge: Lazy[AutoMerge[TailA, TailB]]
  ): AutoMerge[FieldType[Name, HeadA] :: TailA, ListB] =
    AutoMerge.instance[FieldType[Name, HeadA] :: TailA, ListB] { (a, b) =>
      val (bValue, bRemain) = select(b)
      val head: FieldType[Name, HeadA] = field[Name](headMerge(a.head, bValue))
      val tail: TailA = tailMerge.value(a.tail, bRemain)
      head :: tail
    }

  /** Merge an empty HList into a HList. */
  implicit def nilAutoMerge[H <: HList]: AutoMerge[H, HNil] =
    AutoMerge.instance[H, HNil] { (a, b) =>
      a
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
