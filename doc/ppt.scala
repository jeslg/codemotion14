object Slide1{

  val factorial: Int => Int

  val scale: Int => Image => Image

  val animation: Function1[Double, Image]

  val server: Request => State => (Result, State)

  val … << choose your favourite domain >>

}

object Slide2{

  def factorial(i: Int): Int = ???

  def scale(i: Int)(img1: Image): Image

  def animation: Function1[Double, Image]

  def server(req: Request)(implicit ctx: State): (Result, State)

}