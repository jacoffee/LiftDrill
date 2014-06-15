package com.jacoffee.example.util

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by qbt-allen on 14-6-2.
 */

trait NumberHelpers {

}
trait TimeHelpers {
	def formatPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm")
	def formatDate(date: Date) = formatPattern.format(date)
	def formatString(dateLike: String) = formatPattern.parse(dateLike)
}
trait EquationHelpers {
	def isBlank(in: String) = in == null || in.isEmpty
}
object Helpers
	extends NumberHelpers
	with TimeHelpers
	with EquationHelpers {

}
