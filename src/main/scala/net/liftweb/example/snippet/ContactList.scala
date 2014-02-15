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

object ContactList extends DispatchSnippet with Loggable {
	
	def dispatch = {
		case "loginQQ" => loginQQ
		case "loginAfterVerifyCode" => loginAfterVerifyCode 
		case "mailAfterSend" => mailAfterSend
	}

	val loginPageUrl = "https://w.mail.qq.com/cgi-bin/login"
	// val contactPageUrl = "http://w.mail.qq.com/cgi-bin/addr_listall?sid="
	val contactPageUrl = "http://w.mail.qq.com/cgi-bin/addr_listall?flag=star&s=search&folderid=all&pagesize=10&from=today&fun=slock&page=0&topmails=0&t=addr_listall&sid="
	val sendToPageUrl = "http://w.mail.qq.com/cgi-bin/readtemplate?t=compose&"

	def loginResp(qq: String, pwd: String, verifyCode: String = "") = 
		Jsoup.connect(loginPageUrl).
		data("f", "xhtml").
		data("uin", qq).
		data("aliastype", "@qq.com").
		data("mss", "1").
		data("btlogin", "登录").
		data("https", "true").
		data("pwd", pwd).
		data("verifycode", verifyCode).
		execute

	// jump page to verify whether verifycode need
	object jumpPageUrl extends RequestVar(getLoginResp.parse.select("meta").last.attr("content").drop(6))
	object jumpPageCookies extends RequestVar(verifyCodePage.cookies)
 	def verifyCodePage  =  {
		getLoginResp.parse.select("meta").last.attr("content").drop(6)
		Jsoup.connect(jumpPageUrl).execute
	}

	// extract qq user and mailbox addr
	def oldParseCotactAndMailBox(doc: Document) = {
		val contactElems = doc.getElementsByAttributeValue("class", "hr")
		if(contactElems.isEmpty) Nil 
		else {
			val contactLinks = contactElems.get(1).select("a")
			val cotactAndMailBox = for( i <- 0 until contactLinks.size ) yield contactLinks.get(i).text
			cotactAndMailBox.toList.map { contactList => {
					val lastgt = contactList.lastIndexOf(">")
					val lastlt = contactList.lastIndexOf("<")
					(contactList.take(lastlt), contactList.slice(lastlt+1, lastgt))
				}
			}
		}
	}

	def parseMailBox(doc: Document, cookies: java.util.Map[String, String]) = {
		val contactElems = doc.getElementsByAttributeValue("class", "hr")
		if(contactElems.isEmpty) <p> empty </p> 
		else {
			val contactLinks = contactElems.get(1).select("a")
			val cotactAndMailBox = for( i <- 0 until contactLinks.size ) yield contactLinks.get(i).text
			val bb = cotactAndMailBox.toList.map { contactList => {
					val lastgt = contactList.lastIndexOf(">")
					val lastlt = contactList.lastIndexOf("<")
					// (contactList.take(lastlt), contactList.slice(lastlt+1, lastgt))
				def sendFormResponse = Jsoup.
					connect(s"${sendToPageUrl}sid=${cookies.get("msid")}&to=${contactList.slice(lastlt+1, lastgt)}").
					cookies(cookies).
					execute
					
				SHtml.a(() => 
						{ 
							SHtml.ajaxInvoke(() => { 
								val rr = getSendMailForm(sendFormResponse.parse)
								Replace("refresh", <p>{ XhtmlParser(Source.fromString(rr.outerHtml)) }</p>) 
							}).cmd
						},
						Text(contactList)
					)
				}
			}
			bb 
		}
	}
	
	def getAddrList = {
		val contactPageContent = Jsoup.connect(s"${contactPageUrl}${getLoginResp.is.cookies.get("msid")}").
		cookies(getLoginResp.is.cookies).get
		oldParseCotactAndMailBox(contactPageContent)
	}

	object getLoginResp extends RequestVar(
		loginResp("1253246958", "")
	)

	// form to input verifycode
	def getVerifyCodeForm = {
		val verifyCodeForm = verifyCodePage.parse.getElementsByTag("form")
		verifyCodeForm.select("form").removeAttr("action")
		verifyCodeForm.select("form").attr("data-lift", "ContactList.loginAfterVerifyCode")
		verifyCodeForm.select("p.tip").remove
		verifyCodeForm.select("p a").get(0).remove
		verifyCodeForm.select("p").get(2).remove
		<div>
			<div id="loginform">{ XhtmlParser(Source.fromString(verifyCodeForm.outerHtml)) }</div>
			{
				{
					SHtml.a(
						() => { SHtml.ajaxInvoke(() => { SetHtml("loginform", XhtmlParser(Source.fromString(verifyCodeForm.outerHtml))) }).cmd },
						Text("看不清, 换一张"), 
						"id"-> "refresh"
					)
				}
			}
		</div>
	}

	// form to send mail
	def getSendMailForm(doc: Document) = {
		val sendMailForm = doc.getElementsByTag("form")
		sendMailForm.select("form").removeAttr("action")
		sendMailForm.select("form").removeAttr("name")
		sendMailForm.select("form").attr("data-lift", "ContactList.loginAfterVerifyCode")
		sendMailForm
	}

	def loginQQ(xhtml: NodeSeq): NodeSeq = {
		// withVerifyCode display  codepage
		// without verifyCode display addListPage
		val contactList = if( getLoginResp.is.cookies.get("msid") != null ) {
			<div>哈哈</div>
		} else {
			// with verifycode, jump to fill in verify code page
			getVerifyCodeForm
		}
		contactList
	}

	object userCookies extends SessionVar(
		Jsoup.connect("http://w.mail.qq.com/cgi-bin/login").data(getFormDatas).cookies(jumpPageCookies).execute.cookies
	)

	def getFormDatas = {
		JavaConversions.mapAsJavaMap(
			S.request.map { req => req.params.map { param => param._1 -> param._2.head} }.get
		)
	}
	def loginAfterVerifyCode(xhtml: NodeSeq): NodeSeq  = {
		val verifycode = S.param("verifycode").openOr("")
		val content = S.param("content").openOr("")
		if(!verifycode.isEmpty) {
			val contactPageContent = Jsoup.connect(s"${contactPageUrl}${userCookies.is.get("msid")}").cookies(userCookies.is).get
			<div>{ parseMailBox(contactPageContent,  userCookies.is) }</div>
		} else if(!content.isEmpty) {
			Jsoup.connect("http://w.mail.qq.com/cgi-bin/cgi_redirect").data(getFormDatas).cookies(userCookies.is).post
			S.redirectTo("/", () => S.notice("myError", "成功登录"))
		} else {
			xhtml
		}
		/*S.param("verifycode") match {
			case Full(verifycode)  =>  {
				val contactPageContent = Jsoup.connect(s"${contactPageUrl}${userCookies.is.get("msid")}").cookies(userCookies.is).get
				<div>{ parseMailBox(contactPageContent,  userCookies.is) }</div>
			}
			case _ =>  xhtml
		}*/
	}
	
	def mailAfterSend  = {
		println(S.request)
		S.clearFunctionMap
		println("-----------------")
		println(getFormDatas)
		S.param("content") match {
			case Full(content) => {
				Jsoup.connect("http://w.mail.qq.com/cgi-bin/cgi_redirect").data(getFormDatas).cookies(userCookies.is).post
				S.redirectTo("/", () => S.notice("myError", "成功登录"))
			}
			case _ => PassThru
		}
	}

}