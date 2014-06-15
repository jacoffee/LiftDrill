package com.jacoffee.example.snippet

import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.http.{ S, SHtml, DispatchSnippet }
import net.liftweb.http.js.JsCmds.SetValById


/**
 * Created by qbt-allen on 14-4-21.
 */
object Chat extends DispatchSnippet {
	def dispatch = {
		case "frontEnd" => frontEnd
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
