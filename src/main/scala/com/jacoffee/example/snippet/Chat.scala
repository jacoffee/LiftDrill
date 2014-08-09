package com.jacoffee.example.snippet

import java.util.Date
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.http.{ S, SHtml, DispatchSnippet }
import net.liftweb.http.js.JsCmds.SetValById
import com.jacoffee.example.comet.ChatServer
import com.jacoffee.example.comet.Message
import net.liftweb.http.js.jquery.JqJE.JqId
import net.liftweb.http.js.jquery.JqJsCmds.jsExpToJsCmd
import net.liftweb.http.js.jquery.JqJsCmds.JqSetHtml
import scala.xml.Text
import net.liftweb.http.js.{JsMember, JsExp}


/**
 * Created by qbt-allen on 14-4-21.
 */
object Chat extends DispatchSnippet {
	def dispatch = {
		case "frontEnd" 	=> frontEnd
		case "time"		=> time
	}

	def time = {
		"*" #> {
			<p></p>
		}
	}

	def frontEnd = {
		val userName = "Miss X"
		def send {
			val msg = S.param("msg").map(_.trim).openOr("")
			if (msg.nonEmpty) {
				ChatServer ! Message(new Date(), userName, msg)
			}
			JqVal("inp_chat")
			SetValById("inp_chat", "")
		}
		"data-bind=action" #> {
			SHtml.hidden(send _)
		}
	}

}

case class JqVal(id: JsExp) extends JsExp {
	override def toJsCmd = "jQuery('#'+" + id.toJsCmd + ").val()"
}