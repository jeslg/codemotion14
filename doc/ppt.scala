trait Functions{

val factorial: Int => Int

val scale: (Int, Image) => Image

val animation: Function1[Double, Image]

val server: Request => State => (Result, State)

val … << choose your favourite domain >>

}

trait Functions2{

val factorial: Function1[Int, Int]

val scale: Function2[Int, Image, Image]

val animation: Function1[Double, Image]

val server: Function1[Request, Function1[State, (Result, State)]

val … << choose your favourite domain >>

}

trait Defs{

def factorial(i: Int): Int = ???

def scale(i: Int)(img1: Image): Image

def animation: Function1[Double, Image]

def server(req: Request)(implicit ctx: State): (Result, State)

def … << choose your favourite domain >>

}

trait Values{

val i: Int = 3

val image: Image = bitmap(“file.png”)

val state: State = (users, entries)

val users: List[User] = List(user1,user2)

val user1: User = User(name=“juan”, age=21)

val factorial: Int => Int

val scale: Int => (Image => Image)

}

trait Composition1{

def parseInt(i: String): Int = 
  Integer.parseInt(i)

def factorial(n: Int): Int = 
  if (n==0) 1
  else n * factorail(n-1)

def main(s: String): Int = 
  factorial(parseInt(s))

def compose(g: Int => Int, f: String => Int): String => Int = 
  (s: String) =>  g(f(x))

def main2: String => Int = compose(factorial, parseInt)

}


trait Composition2{

def compose[A,B,C](g: B => C, f: A => B): A => C 

trait Function1[-T1, +R] extends AnyRef { 
  def apply(v1: T1): R

  def compose[A](g: A => T1): A => R = { 
    x => apply(g(x)) 
  }

  def andThen[A](g: R => A): T1 => A = { 
    x => g(apply(x)) 
  }

  override def toString() = "<function1>"
}

}


trait SideEffects{

def factorial(n: Int): Int = 
  if (n < 0) 
    throw new IllegalArgumentException
  else {
    val result = if (n==0) 1 else n * factorial(n-1)
    println(s"factorial($n)=$result")
    result
  }  

}


trait SideEffects2{

sealed trait Option[+T]
case object None extends Option[Nothing]
case class Some[+T](value: T) extends Option[T]

sealed trait Logging
case class Debug(msg: String) extends Logging
case class Error(msg: String) extends Logging
case class Multilog(logs: List[Logging]) extends Logging

def parseInt(s: String): (Logging, Option[Int]) = 
  try{
    (Debug(s"Parsing $s"), Some(Integer.parseInt(s)))
  } catch {
    case _: NumberFormatException => 
      (Error(s"$s is not an integer"), None)
  }


def factorial1(n: Int): (Logging, Option[Int]) = 
  if (n < 0) 
    (Error(s"factorial($n) = error: negative number"), None)
  else if (n == 0) 
    (Debug(s"factorial($n)=1"), Some(1))
  else {
    val (logging, maybe_rec_result) = factorial1(n-1)
    maybe_rec_result match {
      case None => 
        (logging, None)
      case Some(rec_result) => 
        val result = n * rec_result 
        val next_logging = Multilog(List(Debug(s"factorial($n)=$result"), logging))
        (next_logging, Some(result))
    }
  }


// def factorial2(n: Int): Option[Int] = {

//   def fact_aux(i: Int, result: Int): Int = 
//     if (i == n) result
//     else fact_aux(i+1, (i+1) * result)

//   if (n < 0) None
//   else Some(fact_aux(0, 1))

// }

// def factorial3(n: Int): (Logging, Option[Int]) = {
  
//   def fact_aux(i: Int, result: Int): (Logging, Int) = 
//     if (i == n) 
//       (Debug(s"factorial($i)=$result"), result)
//     else 
//       fact_aux(i + 1, (i + 1) * result)

//   if (n < 0) 
//     (Error(s"factorial($n) = error: negative number"), None)
//   else {
//     val (logging, result) = fact_aux(0, 1)
//     (logging, Some(result))
//   }

// }

def evaluator[T](result: => Option[T]): Unit = 
  result.fold(println("Invalid computation")){
    result => println(s"Computation result: $result")
  }

def logger(logging: => Logging): Unit = 
  logging match {
    case Debug(msg) => println(msg)
    case Error(msg) => println(msg)
    case Multilog(log :: rest_log) => {
      logger(log)
      rest_log.foreach { log => logger(log) }
    }
    case Multilog(Nil) => ()
  }

def interpreter[T](result: => (Logging, Option[T])): Unit = {
  val (logging, evaluation) = result
  logger(logging)
  evaluator(evaluation)
}

def main: String => (Logging, Option[Int]) = ???

interpreter(main(3))

}


trait ADT{

sealed trait DataType
case object Case1 extends DataType
case class Case2(arg1: T1, arg2: T2, …) extends DataType

// pattern matching

val t: DataType
val v: Int = t match { 
   case c1@Case1 => …:Int
   case c2@Case2(a1,a2,…) => …:Int
}


}

trait Arrows{

type Function[-T,+R]

type KleisliOption[-T,R] = T => Option[R]

type KleisliMyEffect[-T,R] = T => (Logging, Option[R])

type IterateeOption[T,E] = ... 

type List[T] = ... 

type Integer = ... 

def composeK[A,B,C](g: B => Option[C], f: A => Option[B]): A => Option[C] = 
  (a: A) => f(a) match {
    case None => None
    case Some(b) => g(b)
  }

}



}




