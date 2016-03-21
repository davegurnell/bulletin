package object bulletin {
  /** `a merge b` syntax */
  implicit class AutoMergeOps[A](a: A) {
    def merge[B](b: B)(implicit merge: AutoMerge[A, B]): A =
      merge(a, b)
  }
}
