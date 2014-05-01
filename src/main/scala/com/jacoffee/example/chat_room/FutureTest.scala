package com.jacoffee.example.chat_room

import org.jsoup.Jsoup
import scala.util.Random
import scala.concurrent.{ Await, ExecutionContext, Future, future }
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import scala.collection.JavaConversions.asScalaBuffer
import scala.language.postfixOps

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

	// await
	val addFuture: Future[Int] = future {
		1+1
	}

	// Await.result 很好的解决了 future还没有执行完 主线程就已经退出的情况
	//  this is blocking  and blocking is bad
	// how it gets blocked
	// Designates (and eventually executes) a thunk which potentially blocks the calling `Thread`.
	// Clients must use `scala.concurrent.blocking` or `scala.concurrent.Await` instead.
	// def blockOn[T](thunk: =>T)(implicit permission: CanAwait): T
	val result = Await.result(addFuture, 1 second)
	println(" The final Result "+ result)


	// callbacks
	val callBackFuture = future {
		println(" Thread Pool is Using" + Thread.currentThread.getName)
		(1 to 1000).toList.sum
	}

	callBackFuture.onComplete {
		// pass partial func as Params
		case Success(value) => {
			println(" Still In the Same Thread Not Main Thread but can not be guaranteed " + Thread.currentThread.getName)
			println(" Future Finish, now the callback is executing --- " + value)
		}
		case Failure(e) => e.printStackTrace
	}

	// the following codes is to prove  onComplete // on Success // onFailure are not blocking the thread but register a event handler
	//  Because the  Futurereturns eventually, at some nondeterministic time, the “Got th
	// callback” message may appear anywhere in that output
	println("A ....."); Thread.sleep(10)
	println("B ...."); Thread.sleep(10)
	println("C ....."); Thread.sleep(10)


	// do many things together and combine their results
	object Cloud {
		def runAlgorithm(i: Int): Future[Int] = future {
			Thread.sleep(Random.nextInt(500))
			val result = i + 10
			println(s"returning result from cloud: $result")
			result
		}
	}

	println(" Starting Future ")
	val result1 = Cloud.runAlgorithm(10)
	val result2 = Cloud.runAlgorithm(20)
	val result3 = Cloud.runAlgorithm(30)

	println(" Before Comprehension ")
	// the collection type returned by a for comprehension is the same type that you begin with
	val finalResult = for {
		r1 <- result1
		r2 <- result2
		r3 <- result3
	} yield r1 + r2 + r3

	finalResult.onSuccess {
		case value => println(" Final Result "+ value)
	}

	Thread.sleep(1000)
}

