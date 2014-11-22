package programs


/* Side effects */

trait Program2{

  def factorial(n: Int): Int = 
    if (n < 0) 
      throw new IllegalArgumentException
    else {
      val result = if (n==0) 1 else n * factorial(n-1)
      println(s"factorial($n)=$result")
      result
    }  

}
