package com.jacoffee.example.test

import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.JInt
import net.liftweb.json.JsonAST.JString


object JsonAstDrill extends App {
	// concat
	val concact = JsonAST.concat(JInt(1), JString("2"))
	println("  JValue拼接 " + concact)
	// JArray(List(JInt(1), JInt(2)))
	// JArray(List(JInt(1), JString(2)))

	println(" ------------------- ")

	
}