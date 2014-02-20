package net.liftweb.example.snippet

import scala.collection.JavaConversions
import scala.collection.convert.Wrappers
import net.liftweb.http.DispatchSnippet
import net.liftweb.common.Loggable
import scala.xml.NodeSeq
import net.liftweb.http.RequestVar
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import scala.xml.parsing.XhtmlParser
import scala.io.Source
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds.SetHtml
import scala.xml.Text
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SessionVar
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.http.js.JsCmds.Replace
import net.liftweb.util.PassThru
import java.util.{ Map => JMap, HashMap => JHMap}
import org.jsoup.nodes.Element
import org.jsoup.nodes.Attributes
import org.jsoup.parser.Tag
import org.jsoup.nodes.Attribute
import net.liftweb.builtin.snippet.Surround
import org.jsoup.Connection.Method
import org.jsoup.safety.Whitelist
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


object QQMail extends DispatchSnippet {
	
	def dispatch = {
		case "login" => login
		case "contact" => contact
		case "write" => write
		case "send" => send
		case "reuse" => reuse
	}

	val loginPageUrl = "https://w.mail.qq.com/cgi-bin/login"
		
	val contactPageUrl = "http://w.mail.qq.com/cgi-bin/addr_listall?sid="
	val sendToPageUrl = "http://w.mail.qq.com/cgi-bin/readtemplate?t=compose&"
	val sendMailUrl = "http://w.mail.qq.com/cgi-bin/cgi_redirect"

	def reuse = {
		// <a id="writeMail" href="#"  data-lift="QQMail.reuse">Write Mail</a>
		"#writeMail" #>  SHtml.a(
			() => {
				SHtml.ajaxInvoke(() => {
					SetHtml("container", XhtmlParser(Source.fromString(writeMailForm)))
				}).cmd
			},
			Text("Write Mail"),
			"id"-> "rewrite"
		)
	}
	
	object userCookies extends SessionVar[JMap[String, String]](new JHMap())
	object qqAndPwd extends SessionVar(("", ""))

	def mockLogIn(qq: String, pwd: String) =
		Jsoup.connect(loginPageUrl).
		data("f", "xhtml").
		data("uin", qq).
		data("aliastype", "@qq.com").
		data("mss", "1").
		data("btlogin", "登录").
		data("https", "true").
		data("pwd", pwd).
		execute

	def login(xhtml: NodeSeq): NodeSeq = {
		/*val qq = S.param("qq").openOr("")
		val pwd = S.param("pwd").openOr("")
		qqAndPwd(qq, pwd)
		val getLoginResp = mockLogIn(qq, pwd)
		if (qq.nonEmpty && pwd.nonEmpty) {
			if ( getLoginResp.cookies.get("msid") != null ) {
				userCookies(getLoginResp.cookies)
				getContactList
			} else {
				// with verifycode, jump to fill in verify code page
				S.redirectTo("/tencent/verifycode", () => S.notice("verifyCodeForm", getVerifyCodeForm(qq, pwd)))
			}
		} else {
			xhtml
		}*/
		getVerifyCodeForm("1253246958", "aaaa2222")
	}

	// form to input verifycode
	def getVerifyCodeForm(qq: String, pwd: String) = {
			try {
				val redirectToUrl = mockLogIn(qq, pwd).parse.select("meta").last.attr("content").drop(6)
				println(redirectToUrl)
				val verifyCodePage = Jsoup.connect(redirectToUrl).timeout(100000).execute
				val verifyCodeForm = verifyCodePage.parse.getElementsByTag("form")
				verifyCodeForm.select("form").removeAttr("action")
				verifyCodeForm.select("form").attr("action", "/tencent/contact")
				verifyCodeForm.select("p.tip").remove
				verifyCodeForm.select("p a").remove
				verifyCodeForm.select("p").get(2).remove
				<div>
				<div id="loginform">{ XhtmlParser(Source.fromString(verifyCodeForm.outerHtml)) }</div>
				{
						SHtml.a(
							() => {
								SHtml.ajaxInvoke(() => {
									SetHtml("loginform", XhtmlParser(Source.fromString(verifyCodeForm.outerHtml)))
								}).cmd
							},
							Text("看不清, 换一张"),
							"id"-> "refresh"
						)
					}
				</div> 
			} catch {
				case e: Exception => <div>{ println(e.getMessage) }</div>
			}
	}

	def getFormDatas = {
		JavaConversions.mapAsJavaMap(
			S.request.map { req => req.params.filterNot(pair => pair._1 == "to").map { param => param._1 -> param._2.head} }.get
		)
	}
	
	
	def writeMailForm = {
		val writeMail = s"http://mail.qq.com/cgi-bin/readtemplate?sid=${userCookies.is.get("msid")}&t=compose&s=cnew&s=left"
		val getWriteMailPage = Jsoup.connect(writeMail).cookies(userCookies.is).method(Method.GET).execute.parse
		val getSendMailForm = getWriteMailPage.select("form#frm").first
		getSendMailForm.removeAttr("enctype").removeAttr("target").attr("action", "/tencent/mail")
		getSendMailForm.select("table.composetab").remove
		getSendMailForm.select("div#toolbar").remove
		getSendMailForm.select("div.clear").remove
		val wrappForm = getSendMailForm.select("div#sendtimepadding").empty.first
		val formElems = {
			<span>收件人:</span><input type="text" name="to" /><br/>
			<span>主题:</span><input style="word-break:break-all;height:16px;line-height:16px;width:99%;border-width:0;" type="text" name="subject">积分分享计划</input><br/>
			<span>正文:</span><textarea name="content__html" id="content"><div><b style='color:red;'>Hello</b><img src='http://www.baidu.com/img/bdlogo.gif' width='270' height='129' /></div></textarea><br/>
			<input type="submit" value="发送" />
		}
		wrappForm.append(formElems.mkString)
		getSendMailForm.outerHtml
	}

	def contact(xhtml: NodeSeq): NodeSeq  = {
		val verifycode = S.param("verifycode").openOr("")
		
		S.param("verifycode") match {
			case Full(verifycode)  =>  {
				userCookies(Jsoup.connect(loginPageUrl).data(getFormDatas).execute.cookies)
		
		val qqPage = Jsoup.connect(s"http://mail.qq.com/cgi-bin/frame_html?sid=${userCookies.is.get("msid")}")
		.cookies(userCookies.is)
		.method(Method.GET).execute
		
		val qqContactPage = Jsoup.connect(s"http://mail.qq.com/cgi-bin/laddr_list?sid=${userCookies.is.get("msid")}&operate=view&t=contact&view=normal")
		.header("Referer", s"http://mail.qq.com/cgi-bin/frame_html?sid=${userCookies.is.get("msid")}&r=${Math.random.toString}")
		.cookies(userCookies.is)
		.get
		
	/*	val writeMail = s"http://mail.qq.com/cgi-bin/readtemplate?sid=${userCookies.is.get("msid")}&t=compose&s=cnew&s=left"
		val qqWrite = Jsoup.connect(writeMail)
		.cookies(userCookies.is)
		.method(Method.GET).execute*/
		
		//println(qqWrite.body)
		
		qqContactPage.select("script").remove
		// println( qqContactPage.select("div#id").outerHtml)
		val innerList = qqContactPage.select("#out").first.getElementsByAttributeValue("ui-type", "list")/*.select("div li")*/
		// println("NNNNNNNNNNNNNNNNNNNNNNN")
		// println(innerList.outerHtml)
		val lis = innerList.select("div li")
		// println("XXXXXXXXXXXXXXXXX")
		// println(lis)
		
		val pairs = for( i <- 0 until innerList.size) yield (innerList.get(i).select("span.name").html, innerList.get(i).select("span.email").html)
		
		<div> {
			XhtmlParser(
				Source.fromString(writeMailForm)
			)
		}
		</div> 
		} case _ =>  xhtml
		}
	}

	def getContactList = {
		val contactList = Jsoup.connect(s"${contactPageUrl}${userCookies.is.get("msid")}").cookies(userCookies.is).get
		<div id="contact">{ parseMailBox(contactList) }</div>
	}

	def sendFormResponse(mailbox: String) = Jsoup.
			connect(s"${sendToPageUrl}sid=${userCookies.is.get("msid")}&to=${ mailbox }").
			cookies(userCookies.is).
			post

	def parseMailBox(doc: Document) = {
		val contactElems = doc.getElementsByAttributeValue("class", "hr")
		if(contactElems.isEmpty) {
			<div>
				<p class="logintips_error">验证码输入错误</p>
				{ getVerifyCodeForm(qqAndPwd.is._1, qqAndPwd.is._2)}
			</div>
		} else {
			val contactLinks = contactElems.get(1).select("a")
			val cotactAndMailBox = for( i <- 0 until contactLinks.size ) yield contactLinks.get(i).text
			<form action="/tencent/write" method="post">
			{
				cotactAndMailBox.toList.zipWithIndex.map {
					case (contactList, index) => {
						val lastgt = contactList.lastIndexOf(">")
						val lastlt = contactList.lastIndexOf("<")
						<input type="checkbox" name={ index.toString } value={ contactList.slice(lastlt+1, lastgt) }  /><label>{ contactList }</label><br />
					}
					case _ => <div></div>
				} ++
				<input type="submit" value="写邮件" />
			}
			</form>
		}
	}

	def write(xhtml: NodeSeq): NodeSeq = {
		println(getFormDatas)
		val mailBoxList = S.request.map { req => req.params.map { param => param._1 -> param._2.head} }.get.values.toList
		println(mailBoxList)
		XhtmlParser(
			Source.fromString{ getSendMailForm(sendFormResponse( mailBoxList.mkString("", ",", ",") )) }
		)
	}

	// form to send mail
	def getSendMailForm(doc: Document) = {
		try {
			val sendMailForm = doc.select("form#frm").first
			sendMailForm.removeAttr("enctype").removeAttr("target").attr("action", "/tencent/mail")
			sendMailForm.outerHtml 
			
		} catch {
			case e: Exception => "<div>邮件发送表单获取失败</div>"
		}
	}

	def send = {
		val to = S.param("to").openOr("")
		val mailList = if ( to.contains(",")){
			to.split(",").toList
		} else {
			List(to)
		}
		println("Form Datas to Send")
		println(getFormDatas)
		
		val issue = Jsoup.connect(s"http://mail.qq.com/cgi-bin/compose_send?sid=${userCookies.is.get("msid")}")
		.data(getFormDatas)
		.data("to", "我自己的邮箱<361541673@qq.com>;1253246958<1253246958@qq.com>")
		//.data("content__html", "<img src='http://www.baidu.com/img/bdlogo.gif' />")
		.data("t", "compose_send.json")
		/*.data("sendmailname", "361541673@qq.com")
		.data("savesendbox", "1")
		.data("sendname", "周梦林")*/
		.data("resp_charset", "UTF8")
		.cookies(userCookies.is)
		.method(Method.POST).execute
		
		println(" ------issue------")
		
		S.param("content") match {
			case Full(content)  =>  {
				PassThru
			}
			case _ =>  S.redirectTo("/", () => S.notice("success", <p>{ issue.parse.outerHtml }</p>))
		}
	}
}

object mm extends App {
	val aa = Jsoup.connect("https://w.mail.qq.com/cgi-bin/loginpage?f=xhtmlmp&errtype=3&verify=true&clientuin=1253246958&t=&alias=&regalias=&aliastype=@qq.com&autologin=n&spcache=&folderid=&3g_sid=&g_key=&msg=&ppp=a2RzPWZhZBw%3D&autologin=n&mss=1&vurl=http://vc.gtimg.com/BO3MUT6HW51AYDKBSJ7H6DCSKNYYYGW5&vid=BO3MUT6HW51AYDKBSJ7H6DCSKNYYYGW5&vuin=gQRQPvQsqVmKHOkkOZSAHgv-szN87JCe-KWyGgTraak.&tfcont=22%20serialization%3A%3Aarchive%205%200%200%209%200%200%200%208%20authtype%201%208%209%20clientuin%2010%201253246958%209%20aliastype%207%20%40qq.com%206%20domain%206%20qq.com%201%20f%205%20xhtml%203%20uin%2010%201253246958%203%20mss%201%201%207%20btlogin%204%20%E7%99%BB%E5%BD%95%205%20https%204%20true&authtype=8")
	.timeout(100000)
	.execute
	println(aa.parse)
}