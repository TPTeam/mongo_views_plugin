package tp_utils

/*
 * Oggetto per poter importare helper su try catch 
 */
object Tryer {
  
  def tryo[T <: Any](f: => T): Option[T] =
    try
  		Some(f)
  	catch {
  	  case _ : Throwable => None
  	}
  
  def checko[T <: Any](f: => Boolean): Boolean =
    try
  		f
  	catch {
  	  case _ : Throwable => false
  	}
    

}