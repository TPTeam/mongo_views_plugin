package tp_utils

/**
 * Object to be used in order to import helper on "try catch"
 */
object Tryer {

  def tryo[T <: Any](f: => T): Option[T] =
    try
      Some(f)
    catch {
      case _: Throwable => None
    }

  def checko[T <: Any](f: => Boolean): Boolean =
    try
      f
    catch {
      case _: Throwable => false
    }

}