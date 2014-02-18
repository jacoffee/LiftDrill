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

object QQMail extends DispatchSnippet {
	
	def dispatch = {
		case "login" => login
		case "contact" => contact
		case "write" => write
		case "send" => send
	}

	val loginPageUrl = "http://w.mail.qq.com/cgi-bin/login"
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
		val qq = S.param("qq").openOr("")
		val pwd = S.param("pwd").openOr("")
		qqAndPwd(qq, pwd)
		val getLoginResp = mockLogIn(qq, pwd)
		if (qq.nonEmpty && pwd.nonEmpty) {
			if ( getLoginResp.cookies.get("msid") != null ) {
				userCookies(getLoginResp.cookies)
				getContactList(userCookies.is)
			} else {
				// with verifycode, jump to fill in verify code page
				S.redirectTo("/tencent/verifycode", () => S.notice("verifyCodeForm", getVerifyCodeForm(qq, pwd)))
			}
		} else {
			xhtml
		}
	}

	// form to input verifycode
	def getVerifyCodeForm(qq: String, pwd: String) = {
		val redirectToUrl = mockLogIn(qq, pwd).parse.select("meta").last.attr("content").drop(6)
		val verifyCodePage = Jsoup.connect(redirectToUrl).execute
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
	}

	def getFormDatas = {
		JavaConversions.mapAsJavaMap(
			S.request.map { req => req.params.filterNot(pair => pair._1 == "to").map { param => param._1 -> param._2.head} }.get
		)
	}
	def contact(xhtml: NodeSeq): NodeSeq  = {
		val verifycode = S.param("verifycode").openOr("")
		S.param("verifycode") match {
			case Full(verifycode)  =>  {
				userCookies(Jsoup.connect(loginPageUrl).data(getFormDatas).execute.cookies)
				getContactList(userCookies.is)
			}
			case _ =>  xhtml
		}
	}

	def getContactList(cookies: java.util.Map[String, String]) = {
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
		/* val generatedForm =
			"""
				<label for="content">收件人 :</label><input name="to" type="text" class="to" /><br />
				<label for="content">主题 :</label><input name="subject" type="text" class="subject" /><br />
				<label for="content">正文 :</label><input name="content" type="textarea" class="mailbody" /><br />
			""" */
		try {
			val sendMailForm = doc.getElementsByTag("form")
			sendMailForm.select("form").removeAttr("action")
			sendMailForm.select("form").attr("action", "/tencent/mail")
			sendMailForm.select("form").removeAttr("name")
			sendMailForm.first.getElementsByAttributeValue("class", "g").remove
			// sendMailForm.first.select("input[name=content]").first.attr("type", "textarea")
			val sendButton = sendMailForm.first.select("input[value=发送]").first
			sendMailForm.first.select("input[type=submit]").remove
			// sendMailForm.select("p.hr").get(0).empty.append(generatedForm)
			sendMailForm.append(sendButton.outerHtml)
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
		S.param("content") match {
			case Full(content)  =>  {
				mailList.map { mailBox =>
					Jsoup.connect(sendMailUrl).data(getFormDatas).data("to", mailBox).cookies(userCookies.is).post
				}
				userCookies.remove
				S.redirectTo("/", () => S.notice("success", "成功发送邮件"))
			}
			case _ =>  PassThru
		}
	}
}
