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

object QQMail extends DispatchSnippet {
	
	def dispatch = {
		case "login" => login
		case "contact" => contact
		case "write" => write
		case "send" => send
	}

	val loginPageUrl = "https://w.mail.qq.com/cgi-bin/login"
		
	val contactPageUrl = "http://w.mail.qq.com/cgi-bin/addr_listall?sid="
	val sendToPageUrl = "http://w.mail.qq.com/cgi-bin/readtemplate?t=compose&"
	val sendMailUrl = "http://w.mail.qq.com/cgi-bin/cgi_redirect"

	
	
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
			println("codes")
			println(mockLogIn(qq, pwd).parse)
			val redirectToUrl = mockLogIn(qq, pwd).parse.select("meta").last.attr("content").drop(6)
			println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
			println(redirectToUrl)
			val verifyCodePage = Jsoup.connect(redirectToUrl).timeout(0).
			header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0").
			execute
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
			case e: Exception => <p>错误</p>
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
