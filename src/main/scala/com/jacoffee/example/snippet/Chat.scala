package com.jacoffee.example.snippet

import java.util.Date
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.http.{ S, SHtml, DispatchSnippet }
import net.liftweb.http.js.JsCmds.SetValById
import com.jacoffee.example.comet.ChatServer
import com.jacoffee.example.comet.Message


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
			S.param("msg").map { m =>
				ChatServer ! Message(new Date(), userName, m.trim)
			}.openOr {
				SetValById("inp_chat", "")
			}
		}
		"data-bind=action" #> {
			SHtml.hidden(send _)
		}
	}

}
