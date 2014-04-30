package com.jacoffee.example.chat_room

import scala.actors.Actor
import scala.actors.Actor.{ react, sender, actor, receive }

/**
 * Created by qbt-allen on 14-4-28.
 */
object EventBasedActor extends App {

	// an important transfer from e-b-a to t-b-a is the usage of react over receive
	def buildChainActor(count: Int, next: Actor): Actor = {
		val a = actor {
			println(" Current Thread Name " + Thread.currentThread.getName)
			// Current Thread Name ForkJoinPool-1-worker-11
			react {
				case 'Die => {
					val from = sender
					if (next != null) {
						next ! 'Die
						react {
							case 'Ack => from ! 'Ack
						}
					} else from ! 'Ack
				}
			}
		}

		if (count > 0) buildChainActor(count - 1, a)
		else a
	}

	val start = System.currentTimeMillis
	buildChainActor(2, null) ! 'Die

	receive {
		case 'Ack =>
			val end = System.currentTimeMillis
			println("Took " + (end - start) + " ms")
	}

}
