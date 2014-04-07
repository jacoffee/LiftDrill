package com.jacoffee.example.snippet

import scala.xml.NodeSeq
import scala.xml.parsing.XhtmlParser
import scala.io.Source
import net.liftweb.http.DispatchSnippet
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import scala.util.Random
import net.liftweb.http.js.JE.ValById
import net.liftweb.util.Helpers
import net.liftweb.http.js.JsCmds.SetValById
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JE.Call
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.DefaultFormats

object RenderNode extends DispatchSnippet {

	def dispatch = {
		case "node" => node
		case  "button" => button
		case "OnEvent" => OnEvent
		case "ajaxCall" => ajaxCall
		case "jsonCall" => jsonCall
		case "company" => company
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

	def button = {
			def callBack(): JsCmd = {
				JsCmds.Alert(" you press me")
			}
			"button [onclick]" #> SHtml.ajaxInvoke(callBack)
	}

	def OnEvent = {
		val x, y = Random.nextInt(10)
		val sum = x + y
		"p *" #> "What is %d + %d?".format(x,y) &
		"input [onchange]" #> SHtml.onEvent(answer=> {
			if (answer == sum.toString) Alert("you got it")
			else Alert("Sorry you are wrong")
		})
	}

	def ajaxCall = {
		def increment(s: String) = {
			Helpers.asInt(s).map(_ + 1).map(_.toString) openOr s
		}
		JsRaw
		"button [onclick]" #> SHtml.ajaxCall(ValById("num"), n => SetValById("num", increment(n)) )
	}

	case class Question(x: Int, y: Int, z: Int) {
		println("X + Y+ Z" + (x+y+z))
		def valid_? = x + y == z
	}

	def jsonCall = {
			implicit val formats = DefaultFormats
			println("HHHHHHHHHHHHHHH")
			class person {
			  println("JJJJJ")
			}
			def validate(value: JValue) : JsCmd = {
					println("XXX")
					println(value)
				value.extractOpt[Question].map(_.valid_?) match {
				case Some(true) => Alert("Looks good")
				case Some(false) => Alert("That doesn't add up")
				case None => Alert("That doesn't make sense")
			}
}
		"a [onclick]" #> SHtml.jsonCall(Call("sendJsonValue"), v => validate(v))
	}

	def company = {
		val defaultSize = 30
		val content = "做我的根 我翅膀让我飞 也有回去的窝我愿意 我也可以 付出一切 也不会可惜就在一起 看时间流逝 要记得我们相爱的方式"
		"data-bind=company" #> {
			if (content.length> defaultSize) {
				<pre class="company">
					{ content.take(defaultSize) }
					<a class="unfold" href="#">全部展开</a>
				</pre> ++
				<pre class="company" style="display:none">
						{ content }
						<a class="fold" href="#">收起</a>
				</pre>

			} else  <pre>{ content }</pre>

		}

	}
}
