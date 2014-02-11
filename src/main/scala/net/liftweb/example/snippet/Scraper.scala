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
import net.liftweb.example.contact.MockLogin


object Scraper extends DispatchSnippet with Loggable {
	def dispatch = {
		case "wiki"  =>  wiki
		case "stringToHtml"  => stringToHtml 
		case "extractAttr"  => extractAttr
		case "htCity" => htCity
		case "wordsParser" => wordsParser
		case "passThru" => passThru
		case "plain" => plain
		case "ajax" => ajax
		case "jsonForm" => jsonForm
		case "jqDatePicker" => jqDatePicker
		case "programmingLanguages" => programmingLanguages
		case "mailQQ" => mailQQ
		case "webQQ" => webQQ
	}
	val baseUrlOfXJH = "http://xjh.haitou.cc"
	val defaultTimeout = 5000
	val cityLoc = ".city_con li a"
	def buildConnection(baseUrl: String) = {
		Jsoup.connect(baseUrl).timeout(defaultTimeout).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0").get
	} 

	def getCityNamesAndUrls(ele: Document, cityLoc: String) = ele.select(cityLoc).map{ alink => (alink.text, s"${baseUrlOfXJH}${alink.attr("href")}") }.toList
	def getUniversityInCity(cityUrl: String) = {
		val doc = buildConnection(cityUrl)
		doc.getElementById("univercityBox").getElementsByAttributeValueMatching("name", "univercity").map {
			university => (university.text, s"${baseUrlOfXJH}${university.select("a").attr("href")}")
		}.toList
	}
	def wiki(xhtml: NodeSeq): NodeSeq = {
		// connect to certain webpage
		val doc = Jsoup.connect("http://en.wikipedia.org/wiki/Main_Page").get()
		// detect certain elements with jQuery-like style method
		<div class="wikiTitle">
			<p>Top viewed In Wiki This week</p>
		{
			doc.select("#mp-itn b a").map { urlText => 
				<a href={s"http://en.wikipedia.org${urlText.attr("href")}"} title={ urlText.attr("title") } target="_blank">
					{ urlText.html }
				</a> 
			}
		}
		</div>
	}

	def stringToHtml(xhtml: NodeSeq): NodeSeq = {
		val html = "<html><head><title>First parse</title></head><body><p>Parsed HTML into a doc.</p></body></html>"
		val document = Jsoup.parse(html)
		<div>
			{ document }
		</div>
	}

	def extractAttr(xhtml: NodeSeq): NodeSeq = {
		val html = "<p>An <a href='http://example.com/'><b>example</b></a> link.</p>";
		// parse to html
		val doc = Jsoup.parse(html);
		val alink = doc.select("a").first
		println(doc)
		println("-----a---------")
		println(doc.select("a").first)
		// text between <p></p>
		<div class="extractAttr">
			<p>{ html }</p>
			<p>text between: { doc.body.text }</p>
			<p>link url: { alink.attr("href")   } </p>
			<p>link text: { alink.text  }</p>
			<p>linkOuterH: { alink.outerHtml } </p> <!-- <a href='http://example.com/'><b>example</b></a> -->
			<p>linkHtml: { alink.html }</p>  <!-- <b>example</b> -->
		</div>
	} 

	def htCity(xhtml: NodeSeq): NodeSeq = {
		val cityInfo = getCityNamesAndUrls(buildConnection(baseUrlOfXJH),cityLoc).map {
				case (cityText, cityUrl) => {
				<div>
					<a href={ cityUrl } target="_blank" class="city">{ cityText }</a>
					{
							getUniversityInCity(cityUrl).map { case (universityText, universityUrl) =>
								<a href={ universityUrl } target="_blank">{ universityText }&#x0020;&#x0020;</a>
							}
						}
				</div>
				}
				case _ => <p>爬取失败</p>
		}
		<div id="projects">
			<h2>projects</h2>
			{ cityInfo }
		</div>
	}

	def wordsParser(xhtml: NodeSeq): NodeSeq = {
		
		implicit class RichAnalyzer[AnalyzerType <: Analyzer](poorAnalyzer: AnalyzerType) {
			def getTerms(text: String) = {
				val stream = poorAnalyzer.reusableTokenStream("", new StringReader(text))
				val charTermAttribute = stream.addAttribute(classOf[CharTermAttribute])
				Stream.continually((stream.incrementToken, charTermAttribute.toString)).takeWhile(_._1).map(_._2)
			}
		}
		val filename = System.getProperty("user.dir") + System.getProperty("file.separator") + "src\\webapp\\test.txt"
		// lucene smart chinese parse
		// analyzer
		val analyzer = new SmartChineseAnalyzer(Version.LUCENE_34,  WordlistLoader.getWordSet(new File("D:\\stopwords.txt"), "//"))
		// raw analyzer
		<div>
		{
			val fileSrc = Jsoup.parse(new File(filename), "GBK")
			analyzer.getTerms(fileSrc.text).toList.mkString("\r\n")
		}
		</div>
	}

	// 这个地方暗示 我们要灵活一点 不一定 一直是 nodeseq =》 nodeseq
	// 只要返回值是这样的就行了
	def passThru = {
		val immerge_? = Random.nextBoolean
		if(immerge_?) {
			"*" #> Text("Congratulations, you won")
		} else PassThru
		//PassThru 是怎么来就怎么去
		// ClearNodes 是来了就没有了
		// def apply(in: NodeSeq): NodeSeq = NodeSeq.Empty
	}

	// plain old form process
	// In the snippet, we can pick out the value of the field namewith S.param("name"):
	def plain = {
		println(S.param("username").openOr(""))
		println(S.request.toList)
		// List(Req(List(username), Map(username -> List(zhoumenglin)), ParsePath(List(scraper),,true,false), , PostRequest, Full(application/x-www-form-urlencoded)))
		val paramList = for {
			req <- S.request.toList
			params <- req.paramNames
		} yield params
		println(paramList) // List(username)
		S.param("username") match {
			case Full(username)  =>  {
				//S.notice("error", "hello" + username)
				// http://stackoverflow.com/questions/6216964/why-doesnt-s-notice-show-up-after-redirect-in-lift-mvc-v2-3
				// if in this format the message will not emerge on the redirected page cause
				// it S object represent the state of current request
				// The messages that you send are held by a RequestVar in the S object.
				S.redirectTo("/", () => S.notice("myError", "hello" + username))
				// Redirects the browser to a given URL and registers a function that will be executed when the browser
				// accesses the new URL.  url must be part of your web applcation or it will not be
			}
			case _ =>  PassThru
		}
	}

	def ajax = {
		var username = ""
		def process: JsCmd = SetHtml("result", Text(username))
		"@name" #> SHtml.text(username, u => username = u) &
		"button *+" #> SHtml.hidden(() => process) // 其实不用<input type="submit">也是可以的 就像以前一样用button照样可以提交
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
			case JsonCmd("processForm", target, params: Map[String, String], all)=> {
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

	def mailQQ(xhtml: NodeSeq): NodeSeq = {
		S.setHeader("Host", "ssl.ptlogin2.qq.com")
		S.setHeader("Referer", "https://mail.qq.com/cgi-bin/loginpage")
		<form id="loginform" onsubmit="return QMLogin.checkInput();" method="post" name="loginform" target="_self" autocomplete="on">
		<input value="522005705" type="hidden" id="aid" name="aid" />
		<input value="4" type="hidden" id="daid" name="daid" />
		<input value="https://mail.qq.com/cgi-bin/login?vt=passport&amp;vm=wpt&amp;ft=ptlogin&amp;ss=&amp;validcnt=&amp;clientaddr=361541673@qq.com" type="hidden" name="u1" id="u1" />
		<input value="1" type="hidden" name="from_ui" />
		<input value="1" type="hidden" name="ptredirect" />
		<input value="1" type="hidden" name="h" />
		<input value="快速登录" name="wording" id="wording" type="hidden" />
		<input	value="https://mail.qq.com/zh_CN/htmledition/style/fast_login181b91.css" type="hidden" id="css" name="css" />
		<input value="m_ptmail" type="hidden" name="mibao_css" />

    <div class="username"><label class="txt_default" for="uin" id="label_uin" default_txt="邮箱帐号或QQ号码" style="">&nbsp;</label>
<input onchange="QMLogin.judgeVC()" readonly="true" class="login_domain" id="domain" name="u_domain" style="font-size: 18px;" value="@qq.com" type="text" tabindex="-1" />
<input onblur="QMLogin.judgeVC()" class="txt alias" id="uin" name="uin" type="text" tabindex="1" value="361541673" />
<input id="u" name="u" value="361541673@qq.com" type="hidden" />
        <div class="autocomplete" id="auto_container" tabindex="-1" hidefocus="true" style="display: none;"></div>
    </div>
    <div class="password"><label class="txt_default" for="p" id="label_p" default_txt="QQ密码" style="">QQ密码</label><input
            onfocus="QMLogin.judgeVC()" class="txt password" id="p" name="p" type="password" tabindex="2" />

        <div id="capTip" class="captips" style="display: none;">大写锁定已打开</div>
    </div>
    <div class="about_password"><input class="remerber_password" type="checkbox" id="remerber_password"
                                       tabindex="5" /><label for="remerber_password">记住登录状态</label><a
            class="forgetPassword" href="/cgi-bin/loginpage?t=getpwdback" target="_blank">忘记密码？</a></div>
    <div id="divSavePassWarning" class="red" style="display:none;">选择此项后，下次将自动登录邮箱（本机两周内有效）。为了您的信息安全，请不要在网吧或公用电脑上使用。
    </div>
    <div id="verifyinput" class="vfcode" style="display: none;">
        <div class="vfcodeinput"><label class="txt_default" for="verifycode" id="label_verifycode" default_txt="验证码">
            &nbsp;</label><input class="txt" id="verifycode" value="" name="verifycode" type="text" tabindex="4"
                                 placeholder="" maxlength="6" autocomplete="off" />

            <div id="verifytip" class="verifytip" style="display: none;">按右图填写，不区分大小写</div>
        </div>
        <div class="gray vfcode_img" style="">
			<img id="imgVerify" onclick="QMLogin.changeImg();"
                                                   onload="QMLogin.onLoadVC();" alt="验证码" onerror="QMLogin.imgError();"></img>
            <div class="vfcode_change"><a id="verifyshow" href="javascript:QMLogin.changeImg();" style="display: none;">看不清楚？换一个</a>
            </div>
        </div>
    </div>
    <div class="login_submit" style=""><a class="login_btn_wrapper" href="javascript:;">
        <input class="login_btn" id="btlogin" name="btlogin" type="submit"  value="登录" tabindex="5" /></a>
    </div>
	</form>
	}

	def webQQ(xhtml: NodeSeq): NodeSeq = {
	<form method="post"  id="loginForm">
		<div class="content">
			<input type="hidden" name="device" value="" />
            <input type="hidden" value="1392122000" name="ts" id="ts" />
            <input type="hidden" value="" name="p" id="p" />
            <input type="hidden" name="f" value="xhtml" />
			<input type="hidden" name="delegate_url" value="" />
            <input type="hidden"  name="action" value="" />
            <input  type="hidden" name="https" value="true" />
            <input type="hidden" name="tfcont" value="" />
            <div id="validcodeMsg" style="display:none;" class="logintips_error"></div>
            <div class="item">
            <div>邮箱帐号或QQ号码：</div>
            <input type="text" name="uin" size="10" id="uin" class="input_obj" value="361541673" /> @
            <select name="aliastype" class="select_obj">
                <option value="@qq.com">qq.com</option>
                <option value="vip">vip.qq.com</option>
                <option value="fox">foxmail.com</option>
            </select>
        </div>
        <div class="item"><label for="pwd">QQ密码：</label><br />
			<input type="password" name="pwd" id="pwd" size="10" class="input_obj" value="" autocomplete="off" />
		</div>
        <div class="item remember">
			<input type="checkbox" id="remember_obj" name="mss" value="1" checked="true" />
			<label for="remember_obj">记住登录状态</label></div>
        <div class="tool_bar ">
			<input type="submit" value=" 登录 " name="btlogin" class="btn1" id="submitBtn" />
		</div>
		</div>
		</form>
	}
}
