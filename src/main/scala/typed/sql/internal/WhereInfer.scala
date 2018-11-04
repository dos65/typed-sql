package typed.sql.internal

import cats.data.NonEmptyList
import shapeless._
import shapeless.ops.adjoin.Adjoin
import typed.sql.internal.FSHOps.IsFieldOf
import typed.sql.{FSH, WhereClause, ast}

import scala.reflect.ClassTag

trait WhereInfer[A <: FSH, C] {
  type Out
  def mkAst(c: C): ast.WhereCond
  def out(c: C): Out
}

object WhereInfer {

  type Aux[A <: FSH, C, Out0] = WhereInfer[A, C] { type Out = Out0 }

  implicit def forEq[A <: FSH, K, V, T](
    implicit
    wt1: Witness.Aux[T],
    ev1: T <:< Symbol,
    wt2: Witness.Aux[K],
    ev2: K <:< Symbol,
    evev: IsFieldOf[A, T]
  ): Aux[A, WhereClause.Eq[K, V, T], V :: HNil] = {
    new WhereInfer[A, WhereClause.Eq[K, V, T]] {
      type Out = V :: HNil
      def mkAst(c: WhereClause.Eq[K, V, T]): ast.WhereCond = ast.WhereEq(ast.Col(wt1.value.name, wt2.value.name))
      def out(c: WhereClause.Eq[K, V, T]): V :: HNil = c.v :: HNil
    }
  }

  implicit def forLess[A <: FSH, K, V, T](
    implicit
    wt1: Witness.Aux[T],
    ev1: T <:< Symbol,
    wt2: Witness.Aux[K],
    ev2: K <:< Symbol,
    evev: IsFieldOf[A, T]
  ): Aux[A, WhereClause.Less[K, V, T], V :: HNil] = {
    new WhereInfer[A, WhereClause.Less[K, V, T]] {
      type Out = V :: HNil
      def mkAst(c: WhereClause.Less[K, V, T]): ast.WhereCond = ast.Less(ast.Col(wt1.value.name, wt2.value.name))
      def out(c: WhereClause.Less[K, V, T]): V :: HNil = c.v :: HNil
    }
  }

  implicit def forLessOrEq[A <: FSH, K, V, T](
    implicit
    wt1: Witness.Aux[T],
    ev1: T <:< Symbol,
    wt2: Witness.Aux[K],
    ev2: K <:< Symbol,
    evev: IsFieldOf[A, T]
  ): Aux[A, WhereClause.LessOrEq[K, V, T], V :: HNil] = {
    new WhereInfer[A, WhereClause.LessOrEq[K, V, T]] {
      type Out = V :: HNil
      def mkAst(c: WhereClause.LessOrEq[K, V, T]): ast.WhereCond = ast.LessOrEq(ast.Col(wt1.value.name, wt2.value.name))
      def out(c: WhereClause.LessOrEq[K, V, T]): V :: HNil = c.v :: HNil
    }
  }

  implicit def forGt[A <: FSH, K, V, T](
    implicit
    wt1: Witness.Aux[T],
    ev1: T <:< Symbol,
    wt2: Witness.Aux[K],
    ev2: K <:< Symbol,
    evev: IsFieldOf[A, T]
  ): Aux[A, WhereClause.Gt[K, V, T], V :: HNil] = {
    new WhereInfer[A, WhereClause.Gt[K, V, T]] {
      type Out = V :: HNil
      def mkAst(c: WhereClause.Gt[K, V, T]): ast.WhereCond = ast.Gt(ast.Col(wt1.value.name, wt2.value.name))
      def out(c: WhereClause.Gt[K, V, T]): V :: HNil = c.v :: HNil
    }
  }

  implicit def forGtOrEq[A <: FSH, K, V, T](
    implicit
    wt1: Witness.Aux[T],
    ev1: T <:< Symbol,
    wt2: Witness.Aux[K],
    ev2: K <:< Symbol,
    evev: IsFieldOf[A, T]
  ): Aux[A, WhereClause.GtOrEq[K, V, T], V :: HNil] = {
    new WhereInfer[A, WhereClause.GtOrEq[K, V, T]] {
      type Out = V :: HNil
      def mkAst(c: WhereClause.GtOrEq[K, V, T]): ast.WhereCond = ast.GtOrEq(ast.Col(wt1.value.name, wt2.value.name))
      def out(c: WhereClause.GtOrEq[K, V, T]): V :: HNil = c.v :: HNil
    }
  }

  implicit def forLike[A <: FSH, K, T](
    implicit
    wt1: Witness.Aux[T],
    ev1: T <:< Symbol,
    wt2: Witness.Aux[K],
    ev2: K <:< Symbol,
    evev: IsFieldOf[A, T]
  ): Aux[A, WhereClause.Like[K, T], String :: HNil] = {
    new WhereInfer[A, WhereClause.Like[K, T]] {
      type Out = String :: HNil
      def mkAst(c: WhereClause.Like[K, T]): ast.WhereCond = ast.Like(ast.Col(wt1.value.name, wt2.value.name))
      def out(c: WhereClause.Like[K, T]): String :: HNil = c.v :: HNil
    }
  }

  implicit def forIn[A <: FSH, K, V, T](
    implicit
    wt1: Witness.Aux[T],
    ev1: T <:< Symbol,
    wt2: Witness.Aux[K],
    ev2: K <:< Symbol,
    evev: IsFieldOf[A, T],
    ct: ClassTag[V]
  ): Aux[A, WhereClause.In[K, V, T], List[V] :: HNil] = {
    new WhereInfer[A, WhereClause.In[K, V, T]] {
      type Out = List[V] :: HNil
      def mkAst(c: WhereClause.In[K, V, T]): ast.WhereCond = ast.In(ast.Col(wt1.value.name, wt2.value.name), c.v.size)
      def out(c: WhereClause.In[K, V, T]): List[V] :: HNil = c.v :: HNil
    }
  }

  implicit def forAnd[S <: FSH, A, B, AOut, BOut, Joined <: HList](
    implicit
    aInf: WhereInfer.Aux[S, A, AOut],
    bInf: WhereInfer.Aux[S, B, BOut],
    adjoin: Adjoin.Aux[AOut :: BOut :: HNil, Joined]
  ): Aux[S, WhereClause.And[A, B], Joined] = {
    new WhereInfer[S, WhereClause.And[A, B]] {
      type Out = Joined
      def mkAst(c: WhereClause.And[A, B]): ast.WhereCond = ast.And(aInf.mkAst(c.a), bInf.mkAst(c.b))
      def out(c: WhereClause.And[A, B]): Joined = adjoin(aInf.out(c.a) :: bInf.out(c.b) :: HNil)
    }
  }

  implicit def forOr[S <: FSH, A, B, AOut, BOut, Joined <: HList](
    implicit
    aInf: WhereInfer.Aux[S, A, AOut],
    bInf: WhereInfer.Aux[S, B, BOut],
    adjoin: Adjoin.Aux[AOut :: BOut :: HNil, Joined]
  ): Aux[S, WhereClause.Or[A, B], Joined] = {
    new WhereInfer[S, WhereClause.Or[A, B]] {
      type Out = Joined
      def mkAst(c: WhereClause.Or[A, B]): ast.WhereCond = ast.Or(aInf.mkAst(c.a), bInf.mkAst(c.b))
      def out(c: WhereClause.Or[A, B]): Joined = adjoin(aInf.out(c.a) :: bInf.out(c.b) :: HNil)
    }
  }

}
