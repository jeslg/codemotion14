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

trait Composition{

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