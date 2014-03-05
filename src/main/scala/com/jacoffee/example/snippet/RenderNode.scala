package net.liftweb.example.snippet

import scala.xml.NodeSeq
import scala.xml.parsing.XhtmlParser
import scala.io.Source
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds.Alert
object RenderNode extends DispatchSnippet {

	def dispatch = {
		case "node" => node
	}
	def node(xhtml: NodeSeq): NodeSeq = {
		/**
		 * rule 1 contant NodeSeq(<p></p>) && Variable Generating( SHtml.a XhtmlParser) 
		 * will need ++ to combine them
		 * 
		  	<code>
				<p>nihao</p> ++ 
				SHtml.a("Click Me", Alert("Wa Ha Ha"))
		 	</code>
		 */
		/**
		 * rule 2 contant NodeSeq(<p>你好</p>) && contant NodeSeq(<p>周梦林</p>)
		 * will automatically combine no need ++
		 * 
		  	<code>
				<p>你好</p>
				<p>周梦林</p>
		 	</code>
		 */
		/**
		 * rule 3 Variable Generating( SHtml.a ) && Variable Generating( XhtmlParser)
		 * will need ++
		 * 
		  	<code>
				XhtmlParser(Source.fromString("<div>喜洋洋</div>")) ++
				XhtmlParser(Source.fromString("<div>喜洋洋</div>"))
		 	</code>
		 */
		Nil
	}
}