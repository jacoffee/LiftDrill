package com.jacoffee.example.snippet

import java.text.SimpleDateFormat
import java.util.Date
import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.parsing.XhtmlParser
import scala.util.Random
import scala.io.Source
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.WordlistLoader
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import net.liftweb.builtin.snippet.Form
import net.liftweb.builtin.snippet.Tail
import net.liftweb.common.{ Full, Box, Empty, Loggable }
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.BindPlus.nodeSeqToBindable
import net.liftweb.util.{ PassThru, Helpers, JsonCmd }
import net.liftweb.http.{ S, SHtml, DispatchSnippet, RequestVar, SessionVar }
import net.liftweb.http.js.{ JsCmd, JsExp}
import net.liftweb.http.JsonHandler
import net.liftweb.http.js.JsCmds.{SetHtml, Alert, Run, Script, SetValById, Function}
import net.liftweb.http.js.JE.{ JsVar, JsRaw }
import net.liftmodules.widgets.autocomplete.AutoComplete

object LoggedIn extends SessionVar[Box[String]](Empty)

object FormSubmit extends DispatchSnippet with Loggable{

	def dispatch = {
		case "passThru" => passThru
		case "plain" => plain
		case "ajax" => ajax
		case "jsonForm" => jsonForm
		case "likes" => likes
		case "opts" => opts
		case "dropdown" => dropdown
		case "programmingLanguages" => programmingLanguages
	}

	// please ignore the detail about the NodeSeq => NodeSeq
	def passThru = {
		val emerge_? = Random.nextBoolean
		// knowplegde points  CSS Transformers
		// * stands for all the child nodes of the passed xhtml
		if(emerge_?) "*" #> Text("Congratulations, you won")
		else PassThru
		// ClearNodes
		// def apply(in: NodeSeq): NodeSeq = NodeSeq.Empty
	}

	// plain old form process
	// In the snippet, we can pick out the value of the field name with S.param("name"):
	object user extends RequestVar(None: Box[String])
	def plain = {
		//S.request.toList)
		S.param("username") match {
			case Full(username)  =>  {
				/* <ul>
				 *   <li>Redirects the browser to a given URL<li>
				 *   <li>url must be part of your web applcation</li>
				 *   <li>registers a function that will be executed when the browser accesses the new URL</li>
				 * </ul>
				 */
				println("  username  " + username)
				user(Some(username))
			}
			case _ => {
				user(Some("用户没有输入姓名"))
			}
		}
		// notice一般是用于表单提交 数据验证后返回结果提醒的
		val userValue = user.is
		"#action" #> SHtml.hidden(() => S.notice("errorCode", user.is.openOr("什么都没有")))
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
/*	def jqDatePicker = {
		// 页面加载的时候 会执行这个东西 也就是相当于绑定了 datePicker插件
		val addDatePicker = Run("$('#birthday').datepicker({dateFormat: 'yy-mm-dd'});")
		val default = dateFormat.format(new Date())
		S.appendJs(addDatePicker)
		"#birthday" #> SHtml.text("", u => u )
	}*/

	def programmingLanguages = {
		val judege = Script(
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