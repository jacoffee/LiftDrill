package com.jacoffee.example.comet

import java.util.Date
import net.liftweb.actor.LiftActor
import net.liftweb.http.{CometListener, CometActor, ListenerManager}
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.jquery.JqJsCmds.AppendHtml
import net.liftweb.http.js.JsCmds.{SetHtml, Script }
import com.jacoffee.example.util.Helpers.formatDate

/**
 * Created with IntelliJ IDEA.
 * User: Allen
 * Date: 14-6-15
 * Time: 下午4:16
 * To change this template use File | Settings | File Templates.
 */
case class Message(time: Date, user: String, msg: String)
// ListenerManager ---> protected def lowPriority: PartialFunction[Any, Unit] = Map.empty
// trait MapLike[A, +B, +This <: MapLike[A, B, This] with Map[A, B]] extends PartialFunction[A, B]
object ChatServer extends LiftActor with ListenerManager {

	private var msgs = Vector[Message]()
	def createUpdate = msgs
	/**
	 * This method is called when the server received  a message.
	 * We check it's of the right type, append it to our
	 * saved list of messages and then send only the new
	 * message to the listeners.
	 */
	override def lowPriority = {
		case m: Message => {
			msgs :+= m; // add newest msg to the Msg Vector
			updateListeners(m) // when the listener(actor) being registered !!
		}
	}
}

class ChatBrowserComponent extends CometActor with CometListener {
	type ALL = Vector[Message]
	private var msgs: ALL = Vector()
	// This controls which Actor to register with for updates. Typically
	def registerWith = ChatServer
	// listen for messages
	override def lowPriority = {
		case m: Message => {
			msgs :+= m
//  Perform a partial update of the comet component based
// on the JsCmd.  This means that the JsCmd will be sent to
// all of the currently listening browser tabs
			partialUpdate(appendMessage(m))
		}
		case all: ALL =>{
			msgs = all
			reRender()
		}
	}
	def appendMessage(m: Message): JsCmd = AppendHtml("chat-room", buildMessageHtml(m))

	// RenderOut
	def render = {
		Script(
			SetHtml(
				"chat-room",
				msgs.flatMap(buildMessageHtml)
			)
		)
	}

	def buildMessageHtml(m: Message) =
		<div class="chat-room-body clearfix">
			<ul>
				<li>{ formatDate(m.time) }</li>
				<li>{ m.user }</li>
				<li>{ m.msg }</li>
			</ul>
		</div>
}
