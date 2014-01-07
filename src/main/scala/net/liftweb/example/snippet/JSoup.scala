package net.liftweb.example.snippet

import net.liftweb.http.DispatchSnippet
import scala.xml.NodeSeq

object JSoup extends DispatchSnippet {
	def dispatch = {
		case "wiki"  =>  wiki
	}
	
	def wiki(xhtml: NodeSeq): NodeSeq = {
		
		Nil
	}
}





