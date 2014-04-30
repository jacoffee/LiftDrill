package com.jacoffee.example.chat_room

import scala.concurrent.{ ExecutionContext, Future, future}
import scala.util.{Try, Success, Failure}

/**
 * Created by qbt-allen on 14-4-30.
 */
object FutureTest extends App {
	import ExecutionContext.Implicits.global
	val s = "hello"
	val f : Future[String] = future {
		s + "Future!!"
	}

	f onSuccess {
		case msg => println(msg)
	}

	f.foreach(println)
	// pay attention to here
	// as the asynchronousm prop, the future will diverge like grapeshot
	// while the main thread will not wait before the future finish  so the futures are unable to come back  to return before main thread finishes
}
