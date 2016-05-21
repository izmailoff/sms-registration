package com.smsreg.utils

import net.liftweb.common._

object ErrorHandlingHelper {

  def getError[T](box: Box[T]): Option[String] =
    box match {
      case ParamFailure(message, _, _, _) => Some(message)
      case Failure(message, _, _) => Some(message)
      case Empty => None
    }
}
