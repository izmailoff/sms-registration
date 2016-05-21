package com.smsreg.utils

import net.liftweb.util.Props
import net.liftweb.util.Props._

/**
 * Helps to execute code conditionally based on runtime environment, etc.
 */
object ConditionalExecution {

  /**
   * Executes the first function that was defined for the run mode.
   * @param default default value, if no functions were defined for the current run mode.
   * @param fs functions that might be defined for the current run mode.
   * @tparam T
   * @return result of the evaluation, in typical use this would be Unit.
   */
  def runModeExecute[T](default: T, fs: PartialFunction[RunModes.Value, T]*): T =
    fs.toList match {
      case h :: t => if(h.isDefinedAt(Props.mode)) h(Props.mode) else runModeExecute(default, t:_*)
      case Nil => default
    }
}
