//package com.jacoffee.example.test
//
//import net.liftweb.json.JsonAST
//import net.liftweb.json.JsonAST.JInt
//import net.liftweb.json.JsonAST.JString
//import org.apache.commons.collections4.map.LRUMap
//
//
//object JsonAstDrill extends App {
//	// concat
//	val concact = JsonAST.concat(JInt(1), JString("2"))
//	println("  JValue拼接 " + concact)
//	// JArray(List(JInt(1), JInt(2)))
//	// JArray(List(JInt(1), JString(2)))
//
//	println(" ------------------- ")
//
//	val lruMap = new LRUMap[String, String](3)
//	lruMap.put("a", "1")
//	lruMap.put("b", "2")
//	lruMap.put("c", "3")
//	println( lruMap.entrySet )
//	println(lruMap)
//	lruMap.get("a")  // put at the end of map a -> c -> b
//	lruMap.put("d", "4")
//	println("After Used" + lruMap)
//	val mapIterator = lruMap.mapIterator
//	while (mapIterator.hasNext) {
//		println(mapIterator.next)
//	}
//
//}