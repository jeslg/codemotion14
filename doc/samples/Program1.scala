package programs


/* No effects. Function composition. */

trait Program1{ self: Program0 => 

  def composeF[A,B,C](g: B => C, f: A => B): A => C = 
    (a: A) => g(f(a))

  def main1: String => Int = 
    composeF(factorial, parseInt)

  def main2: String => Int = 
    (factorial _) compose parseInt

  def main3: String => Int = 
    (parseInt _) andThen factorial

}

object Program1 extends Program1 with Program0

