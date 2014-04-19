package com.jacoffee.example.snippet

import net.liftweb.http.DispatchSnippet
import net.liftweb.util.Helpers.strToCssBindPromoter

/**
 * Created by qbt-allen on 14-4-19.
 */
object Article  extends DispatchSnippet {
	def dispatch = {
		case "list" => articleList
	}

	def articleList = {

		"*" #> <p></p>
	}

}
