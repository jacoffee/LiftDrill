package com.jacoffee.example.snippet

import scala.xml.{Node, Elem, NodeSeq}
import net.liftweb.http.DispatchSnippet


/**
 * Created by qbt-allen on 14-4-21.
 */
object Default extends DispatchSnippet {
	def dispatch = {
		case "pageTitle" => pageTitle
	}

	def pageTitle = { (xhtml: NodeSeq) => {
			 def selectChildNode(xhtml: NodeSeq) = {
				xhtml flatMap {
					case e:  Node => e.child
					case _ => NodeSeq.Empty
				}
			}
			<head>
				<title> { selectChildNode(xhtml) } - Programming Should Be Fun</title>
				<meta name="keywords" content="Programming, PhotoGraphing, Dancing, Guitar" />
				<meta name="description" content="This is place is where I mark my Growth" />
			</head>
		}
	}
}
