package object bulletin {
  /** `a merge b` syntax */
  implicit class MergeOps[A](a: A) {
    def merge[B](b: B)(implicit merge: Merge[A, B]): A =
      merge(a, b)
  }
}
