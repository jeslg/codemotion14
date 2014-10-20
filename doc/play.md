# Play Types: builders, combinators and wrappers

## Action

```scala
trait Action[A] extends EssentialAction
object Action extends ActionBuilder[Request]

// builders (object Action)
def apply(block: ⇒ Result): Action[AnyContent]
def apply(block: (Request[AnyContent]) ⇒ Result): Action[AnyContent]  
def apply[A](bodyParser: BodyParser[A])(block: (Request[A]) ⇒ Result): Action[A]
def async[A](bodyParser: BodyParser[A])(block: (Request[A]) ⇒ Future[Result]): Action[A] 
def async(block: (Request[AnyContent]) ⇒ Future[Result]): Action[AnyContent] 
def async(block: ⇒ Future[Result]): Action[AnyContent] 

// combinators (trait Action):
def andThen[A](g: (Iteratee[Array[Byte], Result]) ⇒ A): (RequestHeader) ⇒ A 
def compose[A](g: (A) ⇒ RequestHeader): (A) ⇒ Iteratee[Array[Byte], Result]

// wrappers
???
```

## ActionBuilder

```scala
trait ActionBuilder[+R[_]] extends ActionFunction[Request, R] {
  def invokeBlock[A](request: Request[A], block: (R[A]) ⇒ Future[Result]): Future[Result] 
}

// builders
???

// combinators
def andThen[Q[_]](other: ActionFunction[R, Q]): ActionBuilder[Q]
def compose(other: ActionBuilder[Request]): ActionBuilder[R]
def compose[Q[_]](other: ActionFunction[Q, Request]): ActionFunction[Q, R] 

// wrappers
???
```

## ActionFilter

```scala
trait ActionFilter[R[_]] extends ActionRefiner[R, R] {
  def filter[A](request: R[A]): Future[Option[Result]]
}

// builders
???

// combinators
def andThen[Q[_]](other: ActionFunction[R, Q]): ActionFunction[R, Q]
def compose(other: ActionBuilder[R]): ActionBuilder[R]
def compose[Q[_]](other: ActionFunction[Q, R]): ActionFunction[Q, R] 

// wrappers
???
```

## ActionTransformer

```scala
trait ActionTransformer[-R[_], +P[_]] extends ActionRefiner[R, P] {
  def transform[A](request: R[A]): Future[P[A]] 
}

// builders
???

// combinators
def andThen[Q[_]](other: ActionFunction[P, Q]): ActionFunction[R, Q]
def compose(other: ActionBuilder[R]): ActionBuilder[P]
def compose[Q[_]](other: ActionFunction[Q, R]): ActionFunction[Q, P] 

// wrappers
???
```

## BodyParser

```scala
trait BodyParser[+A] extends (RequestHeader) ⇒ Iteratee[Array[Byte], Either[Result, A]] 
object BodyParser
object BodyParsers {
  object parse
}

// builders
def apply[T](debugName: String)(f: (RequestHeader) ⇒ Iteratee[Array[Byte], Either[Result, T]]): BodyParser[T]
def apply[T](f: (RequestHeader) ⇒ Iteratee[Array[Byte], Either[Result, T]]): BodyParser[T]
parse.json
parse.text
parse.xml
parse.error.
parse.file

// combinators
def andThen[A](g: (Iteratee[Array[Byte], Either[Result, A]]) ⇒ A): (RequestHeader) ⇒ A
def compose[A](g: (A) ⇒ RequestHeader): (A) ⇒ Iteratee[Array[Byte], Either[Result, A]]
def flatMap[B](f: (A) ⇒ BodyParser[B])(implicit ec: ExecutionContext): BodyParser[B]
def flatMapM[B](f: (A) ⇒ Future[BodyParser[B]])(implicit ec: ExecutionContext): BodyParser[B]
def map[B](f: (A) ⇒ B)(implicit ec: ExecutionContext): BodyParser[B]
def mapM[B](f: (A) ⇒ Future[B])(implicit ec: ExecutionContext): BodyParser[B]
parse.using
parse.when

// wrappers
???
```

## Future

```scala
trait Future[+T] extends Awaitable[T]
object Future

// builders
def apply[T](body: ⇒ T)(implicit executor: ExecutionContext): Future[T]
def failed[T](exception: Throwable): Future[T]
def successful[T](result: T): Future[T] 
def fold[T, R](futures: TraversableOnce[Future[T]])(zero: R)(op: (R, T) ⇒ R)(implicit executor: ExecutionContext): Future[R]
def reduce[T, R >: T](futures: TraversableOnce[Future[T]])(op: (R, T) ⇒ R)(implicit executor: ExecutionContext): Future[R]
def sequence[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])(implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]], executor: ExecutionContext): Future[M[A]]
def traverse[A, B, M[X] <: TraversableOnce[X]](in: M[A])(fn: (A) ⇒ Future[B])(implicit cbf: CanBuildFrom[M[A], B, M[B]], executor: ExecutionContext): 

// combinators
def andThen[U](pf: PartialFunction[Try[T], U])(implicit executor: ExecutionContext): Future[T]
def filter(p: (T) ⇒ Boolean)(implicit executor: ExecutionContext): Future[T]
def flatMap[S](f: (T) ⇒ Future[S])(implicit executor: ExecutionContext): Future[S]
def foreach[U](f: (T) ⇒ U)(implicit executor: ExecutionContext): Unit
def map[S](f: (T) ⇒ S)(implicit executor: ExecutionContext): Future[S]
def transform[S](s: (T) ⇒ S, f: (Throwable) ⇒ Throwable)(implicit executor: ExecutionContext): Future[S]
def zip[U](that: Future[U]): Future[(T, U)] 

// wrappers
???
```
