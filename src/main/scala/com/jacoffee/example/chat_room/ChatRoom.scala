package com.jacoffee.example.chat_room

import scala.actors.Actor
import scala.actors.Actor.{ receive, sender, reply }
import scala.actors.Actor.actor


/**
 * Created by qbt-allen on 14-4-28.
 */
case  class User(name: String)
case class Subscribe(user: User)
case class Unsubscribe(user: User)
case class Post(msg: String)
case class UserPost(user: User, post: Post)

object ChatRoom  extends App {
	var session = Map.empty[User, Actor]
	val chatRoom = actor {
		while(true) {
			receive {
				case Subscribe(user) => {
					// user send a subscription -- ChatRoom as an Actor has to fetch this msg from mailbox and send a msg back
					// and add this new User in the Session
					println(" Subsription Req Arrives")
					val subscriber = sender
					val sessionUser = actor {
						println(" Test Entry 1")
						while(true) {
							Actor.self.receive {
								// When ChatRoom receives a Subscribe message, it creates a new actor representing the user, and associates the user with the newly created actor.
								// 当聊天室接受到一个注册请求的时候 它会创造一个新的Actor 来代表这个新的用户 并且和 请求注册的User  相关联
								case Post(msg) =>   { // Send message to sender
									println(" The name of Subscriber " + msg)
									subscriber ! Post(msg)
								}
							}
						}
					}
					// 给注册用户发送 反馈
					sessionUser ! Post(user.name)
					session = session + (user -> sessionUser)
					// !? blocks the calling thread until the message is sent and a reply received.
					reply("Subscribed " + user)
				}
				case Unsubscribe(user) => {}
				case UserPost(user, post) => {}
			}
		}
	}
	chatRoom !!  Subscribe(User("Pop"))
}

