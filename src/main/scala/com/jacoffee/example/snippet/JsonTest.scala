package net.liftweb.example.snippet

import scala.io.Source
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.JObject

object JsonTest extends App  {
	// practice the usage of JsonParser
	// get from src  --- reflection
	// JObject(List(JField(11,JString(市场专员)), JField(12,JString(销售助理)), JField(13,JString(人力资源助理))))
	val srcReader = Source.fromInputStream(getClass.getResourceAsStream("/location/job.json")).reader
	val result = JsonParser.parse(srcReader) match {
		case jObject: JObject => jObject
		case other => sys.error(" you should say " + other)
	}
	println(" The finale is " + result)
}

