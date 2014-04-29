package com.jacoffee.example.chat_room

import scala.actors.Actor
import scala.actors.Actor.{ react, sender, actor, receive }

/**
 * Created by qbt-allen on 14-4-29.
 */
case class From(actor: Actor)
case class Put(msg: String)

object ReactTest extends App {
	val helloActor =
		actor {
//			val from = sender
			react {
				case From(actor) => {
					react {
						// execute the first req and will wait for the Put message
						// the second react will block
						case Put(msg) => {
							println(" Actor Info " + actor.getState)
							actor ! msg
						}
					}
				}
			}
		}
	helloActor ! From(Actor.self)
	helloActor ! Put("If msg is for the break of the shackle")
}
