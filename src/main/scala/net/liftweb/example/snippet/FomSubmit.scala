/*
 * Copyright 2010-2013 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb.example.snippet

import scala.collection.JavaConversions.asScalaBuffer
import net.liftweb.http.DispatchSnippet
import scala.xml.NodeSeq
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.WordlistLoader
import org.apache.lucene.analysis.Analyzer
import java.io.StringReader
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import java.io.File
import scala.util.Random
import net.liftweb.util.PassThru
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.builtin.snippet.Form
import net.liftweb.http.S
import scala.xml.Text
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.http.JsonHandler
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.util.JsonCmd
import net.liftweb.http.js.JsCmds.{SetHtml, Script, SetValById, Function}
import net.liftweb.http.js.JE.JsVar
import net.liftweb.http.js.JE.JsRaw
import java.text.SimpleDateFormat
import java.util.Date
import net.liftweb.http.js.JsCmds.Run
import net.liftmodules.widgets.autocomplete.AutoComplete
import net.liftweb.common.Loggable
import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JE
import scala.xml.parsing.XhtmlParser
import scala.io.Source
import net.liftweb.http.SessionVar
import net.liftweb.common.Empty
import net.liftweb.common.Box
import net.liftweb.builtin.snippet.Form
import net.liftweb.http.RequestVar
import net.liftweb.builtin.snippet.Tail

/*
 * object BasicDispatchUsage extends RestHelper {
	serve {
		// 在地址栏输入信息即可访问    thingToResp(b>Static</b>)
		//case "my" :: "sample" :: _ Get _ => <b>Static</b>
		//
	  	case "sample" :: "one" :: _ XmlGet _ => <b>Static</b>
	}
}*/

// 不要放到别的对象里面去了
object LoggedIn extends SessionVar[Box[String]](Empty)

object FormSubmit extends DispatchSnippet with Loggable{

	def dispatch = {
		case "checkLogIn"   =>  checkLogIn _
		case "passThru" => passThru
		case "plain" => plain
		case "plain2" => plain2
		case "likes" => likes
		case "opts" => opts
		case "dropdown" => dropdown
		case "ajax" => ajax
		case "sendMail" => sendMail
		case "jsonForm" => jsonForm
		case "jqDatePicker" => jqDatePicker
		case "programmingLanguages" => programmingLanguages
		case _  => render _
	}

	def checkLogIn(xhtml: NodeSeq): NodeSeq = {
		 transfer(xhtml)
	}

	// The CSS selector functionality in Lift gives you a CssSelfunction, which is  NodeSeq => NodeSeq
	 val transfer = "name=username"  #> SHtml.text("", username => LoggedIn(Box.!!(username))) & "type=submit"  #> SHtml.onSubmitUnit(process)

	def process() = {
		S.notice(<p>登陆成功</p>)
		S.redirectTo("/index")
	 }

	def render(xhtml: NodeSeq): NodeSeq  =  {
		<div>This snippet evaluated on  { Thread.currentThread.getName }  </div>
	}

	// 这个地方暗示 我们要灵活一点 不一定每一次都直接写  nodeseq => nodeseq
	// 只要从本质上是 NodeSeq => NodeSeq 就行了
	def passThru = {
		val emerge_? = Random.nextBoolean
		// knowplegde points  CSS Transformers
		// * stands for all the child nodes of the passed xhtml
		if(emerge_?) "*" #> Text("Congratulations, you won")

		else PassThru
		// ClearNodes 是来了就没有了
		// def apply(in: NodeSeq): NodeSeq = NodeSeq.Empty
	}

	// plain old form process
	// In the snippet, we can pick out the value of the field name with S.param("name"):
	object user extends RequestVar[String]("")
	def plain = {
		println(S.request.toList) //  List(Req(List(), Map(), ParsePath(List(formsubmit),,true,false), , GetRequest, Empty))
		// check whether the form has some params
		println(S.param("username").openOr(""))
		println("88888888")
		println(user.is)
		println("88888888")
		val paramList = for {
			req <- S.request.toList
			params <- req.paramNames
		} yield params
		// println(paramList) // List(username)
		S.param("username") match {
			case Full(username)  =>  {
				user(username)
				// S.notice("error", "hello" + username)
				// http://stackoverflow.com/questions/6216964/why-doesn't-s-notice-show-up-after-redirect-in-lift-mvc-v2-3
				// if in this format the message will not emerge on the redirected page cause
				// it S object represent the state of current request
				// The messages that you send are held by a RequestVar in the S object.
				S.redirectTo("/formsubmit", () => {
					//println(S.request) // a new request identified by assgined var lift_page
					S.notice("myError", "hello" + username)
				})
				/* <ul>
				 *   <li>Redirects the browser to a given URL<li>
				 *   <li>url must be part of your web applcation</li>
				 *   <li>registers a function that will be executed when the browser accesses the new URL</li>
				 * </ul>
				 */
			}
			case _ => PassThru
		}
	}

	def plain2 = {
		println("XXXXX")
		println(user.is)  // can not fetch cause in different request
		println("-----")
		S.param("password") match {
			case Full(password)  =>  {
				S.redirectTo("/", () => {
					println("UUUUUU")
					println(user.is)  // can not fetch cause in different request
					println("OOOOOOOOO")
					S.notice("myError", "hello" + password)
				})
			}
			case _ => PassThru
		}
	}

	def likes = {
		var likesTurtles = false
		def disbale = {
			if( Math.random > 0.5d ) "* [disabled]" #> "disable"
			else PassThru
		}
		val possible = List("apple", "banana", "pear")
		var actual = List("")
		def receive(values: Seq[String]) = {
			println(values)
			<p>{ values }</p>
		}
		// 这种情况用于判断 某个选项是否选择 以进行后续的操作
		"type=submit"  #> SHtml.onSubmitUnit( () => println("<><><><><>" +likesTurtles) )
		//"#like" #> SHtml.checkbox(possible, actual, receive(actual))
	}

	def opts = {
		val opts = List(("apple", "apple"),("banana", "banana"),("pear", "pear"))
		val default = List("apple")
		"#opts" #> SHtml.multiSelect(opts, default, opts => println(opts)) &
		 "type=submit"  #> SHtml.onSubmitUnit( () => println(default) )
	}

	def dropdown = {
		def getSelected(selected: String) = println( s"you choose${selected}")
		val options = List(("shidanwh@qq.com", "julia<shidanwh@qq.com>"),("837265033@qq.com", "°★·°颍°<837265033@qq.com>"))
		val default = Empty
		def logSelected() = println("Values selected: "+ default)

		"#dropdown" #> SHtml.selectObj(options, default, getSelected, "id" -> "dropdown") &
		"type=submit"  #> SHtml.onSubmitUnit( logSelected )
	}

	def ajax = {
		var username = ""
		def process: JsCmd = SetHtml("result", Text(username))
		"@name" #> SHtml.text(username, u => { 
			println("hello "+ username) 
			username = u
		}) &
		"button *+" #> SHtml.hidden(() => process)
		// *+ stands for append the content on the right of #> to the last child node of button elem
		// 其实不用<input type="submit">也是可以的 就像以前一样用button照样可以提交
	}

	 def sendMail(xhtml: NodeSeq): NodeSeq = {
		 println("请求结果")
		 println(S.request)
		 var content = ""
		 def process ={
			 println("提交了")
			 SetHtml("content", 
				 <div id="mainbody">
				 	<b style='color:red;'>Hello</b><img src='http://www.baidu.com/img/bdlogo.gif' width='270' height='129' />
				 </div>
			 )
		 }
		/* "#form" #> SHtml.ajaxForm(<div id="mainbody">
				<b style='color:red;'>Hello</b><img src='http://www.baidu.com/img/bdlogo.gif' width='270' height='129' />
			</div>
			<span>正文:</span><textarea name="content_html" id="content" style="display:none"></textarea><br/>
			<input type="submit" />, process, Alert("成功了"))*/
		 
		  S.param("content_html") match {
		 	  case Full(content_html)  => {
		 	 	  println("---------------------")
		 	 	  println(content_html)
		 	 	 <div>
		 	 	  	{ XhtmlParser(Source.fromString(content_html.trim)) } 
		 	 	  	<p style="color:red">奥迪现存的汽车外形设计风格是谁奠定的，其中又有何故事？</p>
		 	 	 </div> 
		 	  }
		 	  case _  => <div>{ xhtml }</div>
		  }
		  
	 }
	
	def jsonForm = {
		// Script的含义可以理解为 在服务器端动态的生成 JS 函数
		"#jsonForm" #> ( (ns: NodeSeq) => SHtml.jsonForm(MotoServer, ns)) &
		"#jsonScript" #> Script(
			MotoServer.jsCmd &
			Function("changeCase", List("direction"),
				MotoServer.call("processCase", JsVar("direction"),
				JsRaw("$('#motto').val()")) // 获取motto的val 并且提交
			)
		)

		/*
			// <![CDATA[
			function F368303627065RF34HS(obj) {liftAjax.lift_ajaxHandler('F368303627065RF34HS='+
										encodeURIComponent(JSON.stringify(obj)), null,null);}
				function changeCase(direction) {
					F368303627065RF34HS({'command': "processCase", 'target': direction, 'params':$('#motto').val()});
				}
			// ]]>
		*/
		// The jsonScript element is bound to JavaScript that will perform the transmission and
		// encoding of the values to the server.
		// 当单击发送的时候  Script中的命令就会被执行 这一段相当于以前放在
	}
	// F3733121248364SIULY:{"command":"processForm","params":{"name":"Royal Society","motto":"Nullius in verba"}}
	// MotoServer 有点类似于以前的 LoginAction 处理Json请求
	
	object MotoServer extends JsonHandler {
		// in 数据格式验证 如果是处理Json的命令就如何
		def apply(in: Any): JsCmd =  in match {
			case JsonCmd("processForm", target, params: Map[String, String] @unchecked, all)=> {
				val name = params.getOrElse("name", "no name")
				val motto = params.getOrElse("motto", "No Motto")
				SetHtml("jsonResult", Text(name + " : " + motto))
			}
			case JsonCmd("processCase", direction, motto: String, all) =>
				val update = if (direction == "upper") motto.toUpperCase
				else motto.toLowerCase
				SetValById("motto", update)
		}
	}
	// 原理解释
	/*
		The  jsonForm method is arranging for form fields to be encoded as JSON
		and sent, via Ajax, to our  MottoServer  as a  JsonCmd.
		In fact, it’s a  JsonCmd  with a default command name of "processForm".
	*/
	private def dateFormat = new SimpleDateFormat("yyyy-MM-dd")
	private def stringToDate(dateLike: String) = dateFormat.parse(dateLike)
	def jqDatePicker = {
		// 页面加载的时候 会执行这个东西 也就是相当于绑定了 datePicker插件
		val addDatePicker = Run("$('#birthday').datepicker({dateFormat: 'yy-mm-dd'});")
		val default = dateFormat.format(new Date())
		S.appendJs(addDatePicker)
		"#birthday" #> SHtml.text("", u => u )
	}

	def programmingLanguages = {
		Script(
			Run(""" if(5>3) window.alert("haha") else window.alert("xixi") """ )
		)
		val default = ""
		val languages = List(
			"C", "C++", "Clojure", "CoffeeScript",
			"Java", "JavaScript",
			"POP-11", "Prolog", "Python", "Processing",
			"Scala", "Scheme", "Smalltalk", "SuperCollider"
		)
		def suggest(fillin: String, limit: Int) = {
			languages.filter(_.toLowerCase.startsWith(fillin))
		}
		def submit(value: String) : Unit = println("Value submitted: "+ value)
		S.appendJs(
			Run(
				"""
				$('#autocomplete input[type=text]').bind('blur', function(){
					$(this).next().val($(this).val());
				});
				"""
			)
		)
		"#autocomplete" #> AutoComplete(default, suggest, submit)
	}
}