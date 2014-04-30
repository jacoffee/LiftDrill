package com.jacoffee.example.chat_room

import org.jsoup.Jsoup
import scala.concurrent.{ ExecutionContext, Future, future}
import scala.util.{Try, Success, Failure}
import scala.collection.JavaConversions.asScalaBuffer

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

	Thread.sleep(1000)
	// pay attention to here
	// as the asynchronousm prop, the future will diverge like grapeshot
	// while the main thread will not wait before the future finish  so the futures are unable to come back  to return before main thread finishes


	//   the following two example is to test the essence of Future
	// concurrently execute some task with returning value(usually, it is the case)
	// on the bottom is the thread-pool sending/dispatching  thread to finish these task
	// so to say the basic component finishing task is  Thread -- the hard t understand but very versatile thread


}

