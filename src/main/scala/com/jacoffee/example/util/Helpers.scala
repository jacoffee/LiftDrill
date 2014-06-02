package com.jacoffee.example.util

/**
 * Created by qbt-allen on 14-6-2.
 */

trait NumberHelpers {

}
trait TimeHelpers {

}
trait EquationHelpers {
	def isBlank(in: String) = in == null || in.isEmpty
}
object Helpers
	extends NumberHelpers
	with TimeHelpers
	with EquationHelpers {

}
