package com.jacoffee.example.daily_fix

import scala.io.Source

object ResourceTest extends App {

	//这里的absolute和relative有一个大前提  就是放在resources目录下才行

	// From ClassLoader, all paths are "absolute" already - there's no context
	// from which they could be relative. Therefore you don't need a leading slash.
	val absResourceStream = this.getClass.getClassLoader.getResourceAsStream("props/mongo.props")
	println(absResourceStream)
	val absBufferedSrc = Source.fromInputStream(absResourceStream, "utf-8").getLines.toList
	println(" absolutely to classLoader")
	println(absBufferedSrc)

	//  From Class, the path is relative to the package of the class unless
	// you include a leading slash, so if you don't want to use the current package, include a slash like this:
	val resourceStream = this.getClass.getResourceAsStream("/test/test.txt")
	println(resourceStream)
	val bufferedSrc = Source.fromInputStream(resourceStream, "utf-8").getLines.toList
	println(" relative to class ")
	println(bufferedSrc)

	//如果不放在resources目录 好像就不能使用 getResourceAsStream
	/*
		val resourceStream1 = this.getClass().getResourceAsStream("/main/webapp/crawler/scraper.html")
		println(resourceStream1
	)*/
	// 使用别的方法 虽然比较原始 但起码解决了问题
	Source.fromFile(System.getProperty("user.dir") + System.getProperty("file.separator") +"src/main/webapp/crawler/scraper.html",  "utf-8")
}
