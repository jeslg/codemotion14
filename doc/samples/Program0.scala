package programs

/* No effects. No combinators */

trait Program0{

  def parseInt(s: String): Int = 
    Integer.parseInt(s)

  def factorial(n: Int): Int = 
    if (n == 0) 1
    else n * factorial(n-1)

  def main(s: String): Int = 
    factorial(parseInt(s))

  def main: String => Int = 
    (s: String) => factorial(parseInt(s))

}

object Program0 extends Program0
