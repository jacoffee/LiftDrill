package net.liftweb.example.snippet

import scala.collection.JavaConversions._
import scala.collection.JavaConversions
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
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SessionVar
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.util.PassThru
import org.jsoup.Connection.Method
import net.liftweb.http.DispatchSnippet
import scala.xml.NodeSeq
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import net.liftweb.util.PassThru
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.builtin.snippet.Form
import net.liftweb.http.S
import scala.xml.Text
import net.liftweb.http.SHtml
import net.liftweb.util.JsonCmd
import net.liftweb.http.js.JsCmds.{SetHtml, Script, SetValById, Function}
import net.liftweb.http.js.JE.JsVar
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JE
import scala.xml.parsing.XhtmlParser
import scala.io.Source
import net.liftweb.common.Empty
import net.liftweb.common.Box
import net.liftweb.builtin.snippet.Form
import net.liftweb.http.RequestVar
import net.liftweb.builtin.snippet.Tail
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.HtmlFixer
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.BindPlus.nodeSeqToBindable

object QQMail extends DispatchSnippet {
	
	def dispatch = {
		case "login" => login
		case "relogin" => relogin
		case "write" => write
		case "send" => send
		case "contact" => contact
	}
	
	case class JsHtml(node: NodeSeq) extends  JsExp with HtmlFixer {
		def toJsCmd = fixHtmlAndJs("inline", node)._1
	}

	val loginPageUrl = "https://w.mail.qq.com/cgi-bin/login"
	val sendMailUrl = "http://mail.qq.com/cgi-bin/compose_send?sid="
	def getSubmittedDatas = {
		S.request.map { req => req.params.filterNot(pair => pair._1 == "to" ).map { param => param._1 -> param._2.head} }.get
	}

	def login_?(cookies: Map[String, String]) = cookies.get("msid").nonEmpty
	object userCookies extends SessionVar[Map[String, String]](Map())
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
				
			} else {
				// with verifycode, jump to fill in verify code page
				S.redirectTo("/tencent/verifycode", () => S.notice("verifyCodeForm", getVerifyCodeForm(qq, pwd)))
			}
		} else {
			xhtml
		}*/
		getVerifyCodeForm
	}

	// form to input verifycode
	def getVerifyCodeForm = {
		def getVerifyCodePageContent(url: String) = 
			Jsoup.connect(url).
			userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36").
			timeout(100000).
			data("uin", "1253246958").
			data("pwd", "aaaa2222").
			execute

		def getProcessedForm(doc: Document) = {
			val verifyCodeForm = doc.getElementsByTag("form")
			verifyCodeForm.select("form").removeAttr("action")
			verifyCodeForm.select("form").attr("action", "/tencent/verifycode")
			verifyCodeForm.select("p.tip").remove
			verifyCodeForm.select("p a").remove
			verifyCodeForm.select("p").get(2).remove
			verifyCodeForm
		} 
		
		try {
			val verifyCodePageUrl = mockLogIn("1253246958", "aaaa2222").parse.select("meta").last.attr("content")
			
			println(verifyCodePageUrl)
			
			val cleanedVerifyCodeForm = getProcessedForm(getVerifyCodePageContent(verifyCodePageUrl).parse)
			<div>
				<div id="loginform">{ XhtmlParser(Source.fromString(cleanedVerifyCodeForm.outerHtml)) }</div>
				{
					SHtml.a(
						() => {
							SHtml.ajaxInvoke(() => {
								SetHtml("loginform", XhtmlParser(Source.fromString(cleanedVerifyCodeForm.outerHtml)))
							}).cmd
						},
						Text("看不清, 换一张"),
						"id"-> "refresh"
					)
				}
				</div> 
		} catch {
			case e: Exception => 
				<div>
					验证码页面加载出错, 请重新尝试或选择其它积分分享方式
					<a href="/">返回</a>
				</div>
		}
	}

	def relogin  = {
		//如果验证码错误返回的结果
		val login = Jsoup.connect(loginPageUrl).data(getSubmittedDatas).timeout(10000).execute
		if (login_?(login.cookies.toMap)) {
			userCookies(login.cookies.toMap)
			S.redirectTo("/tencent/write")
		} else {
			"*" #> getVerifyCodeForm & "#errorVerifycode" #> "验证码错误"
		}
	}

	
	def renderSendMailForm(passedNodeSeq: NodeSeq): String = {
		val writeMailUrl = s"http://mail.qq.com/cgi-bin/readtemplate?sid=${setCookies}&t=compose&s=cnew&s=left"
		val getWriteMailPage = Jsoup.connect(writeMailUrl).cookies(userCookies.is).method(Method.GET).execute.parse
		val getSendMailForm = getWriteMailPage.select("form#frm").first
		getSendMailForm.removeAttr("enctype").removeAttr("target").attr("action", "/tencent/mail")
		getSendMailForm.select("table.composetab").remove
		getSendMailForm.select("div#toolbar").remove
		getSendMailForm.select("div.clear").remove
		val wrappForm = getSendMailForm.select("div#sendtimepadding").empty.first
		wrappForm.append(passedNodeSeq.mkString)
		getSendMailForm.outerHtml
	}

	def write(xhtml: NodeSeq): NodeSeq = {
		val msid = userCookies.is.get("msid")
		msid match {
			case Some(msid) => {
				XhtmlParser( Source.fromString(renderSendMailForm(xhtml)) )
			}
			case _ => S.redirectTo("/tencent/login")
		}
	}

	def send = {
		val to = S.param("to").openOr("")
		val mailList = if ( to.contains(";")){ to.split(";").toList } else { List(to) }
		mailList.map { mailBox =>
			Jsoup.connect(s"${sendMailUrl}${setCookies}")
			.data(getSubmittedDatas)
			.data("to", mailBox)
			.data("t", "compose_send.json")
			.data("resp_charset", "UTF8")
			.cookies(userCookies.is)
			.method(Method.POST).execute
		}
		S.redirectTo("/", () => S.notice("success", <p>成功</p>))
	}

	def contact(xhtml: NodeSeq): NodeSeq = {
		def getQQAndMailBox = {
			val qqContactPage = Jsoup.connect(s"http://mail.qq.com/cgi-bin/laddr_list?sid=${setCookies}&operate=view&t=contact&view=normal")
			.header("Referer", s"http://mail.qq.com/cgi-bin/frame_html?sid=${setCookies}&r=${Math.random.toString}")
			.timeout(10000)
			.cookies(userCookies.is)
			.get
			qqContactPage.select("script").remove
			val allContactLi = qqContactPage.select("#out #list").first.getElementsByAttributeValue("ui-type", "list").select("div li")
			for( i <- 1 until allContactLi.size) yield (allContactLi.get(i).select("span.name").html, allContactLi.get(i).select("span.email").html)
		}
		
		try {
			<div id="addrList">
				<div>
					<input type="checkbox" class="checkAll" />
					<span>联系人列表</span>
				</div> 
				{
					getQQAndMailBox.map {
						case (qq, mail) => {
							<div class="mui_li">
								<div class="mui_li_con">
									<div class="mui_nowrap m_title">
										<a href="#">{ qq }</a>
									</div>
									<div class="mui_nowrap m_summary">{ mail }</div>
								</div>
								<div class="mui_li_front">
									<div class="m_check" style="margin: 0 0 0 -20px;">
										<input type="checkbox" value={ s"${qq}<${mail}>"} />
									</div>
								</div>
							</div>
						}
						case _ => <div>QQ联系人加载失败</div>
					}
				} 
			</div>
		} catch {
			case e: Exception => <div>QQ联系人加载失败</div>
		}
	}

	def setCookies = userCookies.is.get("msid").getOrElse("")
	
	def getAllConactList = {
		def getQQAndMailBox = {
			val qqContactPage = Jsoup.connect(s"http://mail.qq.com/cgi-bin/laddr_list?sid=${setCookies}&operate=view&t=contact&view=normal")
			.header("Referer", s"http://mail.qq.com/cgi-bin/frame_html?sid=${setCookies}&r=${Math.random.toString}")
			.timeout(10000)
			.cookies(userCookies.is)
			.get
			qqContactPage.select("script").remove
			val allContactLi = qqContactPage.select("#out #list").first.getElementsByAttributeValue("ui-type", "list").select("div li")
			for( i <- 1 until allContactLi.size) yield (allContactLi.get(i).select("span.name").html, allContactLi.get(i).select("span.email").html)
		}
		try {
			<div id="addrList">
			{
				getQQAndMailBox.map {
					case (qq, mail) => {
						<div class="mui_li">
							<div class="mui_li_con">
								<div class="mui_nowrap m_title">
									<a href="#">{ qq }</a>
								</div>
								<div class="mui_nowrap m_summary">{ mail }</div>
							</div>
							<div class="mui_li_front">
								<div class="m_check">
									<input type="checkbox" value={ s"${qq}<${mail}>"} />
								</div>
							</div>
						</div>
					}
					case _ => <div>QQ联系人加载失败</div>
				}
			} 
			<div class="operation"><button id="makesure">确定</button></div>
		</div>	
		} catch {
			case e: Exception => <div>联系人获取失败</div>
		}
	}

}