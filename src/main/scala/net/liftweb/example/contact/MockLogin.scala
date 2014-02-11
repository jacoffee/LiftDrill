package net.liftweb.example.contact

import org.apache.http.client.methods.HttpPost
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.protocol.HTTP
import collection.JavaConversions._
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.Header
import org.apache.http.message.BasicHeader

class MockLogin {
	// mock request message
	// login url
	val LOGINURLOFREN = "http://www.renren.com/PLogin.do"
	val LOGINURLOFQQ = "http://mail.qq.com/cgi-bin/loginpage"
	val redirectURL = "http://blog.renren.com/blog/304317577/449470467"
	// 登陆请求

	// All the parameters post to the web site
    val nvps = List[NameValuePair]()
	def RENREN  = 
		new BasicNameValuePair("origURL", redirectURL) ::
        new BasicNameValuePair("domain", "renren.com") ::
        new BasicNameValuePair("isplogin", "true") ::  
        new BasicNameValuePair("formName", "") ::
        new BasicNameValuePair("method", "post") :: 
        new BasicNameValuePair("submit", "登录") :: 
        new BasicNameValuePair("email", "361541673@163.com") :: 
        new BasicNameValuePair("password", "729he514")::Nil
        
    def QQ = 
    	new BasicNameValuePair("origURL", LOGINURLOFQQ) ::
        new BasicNameValuePair("domain", "mail.qq.com") ::
        new BasicNameValuePair("method", "post") :: 
        new BasicNameValuePair("submit", "登录") :: 
        new BasicNameValuePair("email", "361541673@qq.com") :: 
        new BasicNameValuePair("password", "729and514")::Nil
    
	// The HttpClient is used in one session
    var response: HttpResponse  = null  
    val httpclient = new DefaultHttpClient  

    // 单击登录
    def clickAndVerify =  new BasicNameValuePair("origURL", LOGINURLOFQQ) :: Nil

    def qqMailLoginPage = {
		// 进入登入页面 获取 验证码
		
	} 

	def sendRequest(headers: List[BasicHeader], url: String) = {
		val httpget = new HttpGet(url)
		println("请求参数")
		try {
			httpget.setHeaders(headers.toArray)
		} catch {
			case e:Exception => false 
		} 
		httpget
	}

	def receiveResponse(httpget: HttpGet) = {
		try {
			val responseHandler = new BasicResponseHandler
			 var responseBody = httpclient.execute(httpget, responseHandler)
			response = httpclient.execute(httpget)  
			response.getAllHeaders.foreach(header => println( <p>{ header.getName +" : "+ header.getValue } </p> )  )
			println(responseBody.substring(responseBody.indexOf("联系人"), (responseBody.length-1) ))
			response
		} finally {
			httpget.abort
		}
	}

	def getText(redirectLocation: String) = {  
        val httpget = new HttpGet(redirectLocation)
        // Create a response handler
        val responseHandler = new BasicResponseHandler  
        var responseBody = ""  
        try {  
        	// 获取登陆之后的网页内容
            responseBody = httpclient.execute(httpget, responseHandler)  
        } catch  {  
        	case e: Exception => responseBody   
        } finally {  
            httpget.abort
            httpclient.getConnectionManager.shutdown  
        }  
        responseBody
    }  
}

object MockLogin extends MockLogin with App {
	// 在输入用户名和密码的过程中会发送一个请求
	// 这一步的操作是为了获取 验证用户名和密码的js
	// Set-Cookie:ptisp=ctc; 这个Cookie在下一步操作的时候会用到

	 def checkHeaders = 
	 	new BasicHeader("origURL", LOGINURLOFQQ) ::
	 	new BasicHeader("domain", "ssl.ptlogin2.mail.qq.com") ::
	 	Nil

	def checkUrl  = 
		"https://ssl.ptlogin2.qq.com/check?"+
		"uin=361541673@qq.com&"+
		"appid=522005705&"+
		"ptlang=2052&" +
		"js_type=2&" +
		"js_ver=10009&" +
		s"r=${Math.random}" 
	// 发送这个请求 主要是为了获取一个 Set-Cookie : ptisp=ctc  ptisp=ctc  P3P  :  CP="CAO PSA OUR"
	val checkRequest = sendRequest(checkHeaders, checkUrl)
	// val checkResp = receiveResponse(checkRequest)
	// val crossArea = checkResp.getFirstHeader("P3P")
	// val cookies = checkResp.getHeaders("Set-Cookie").foreach(header => println(header.getName + " : " + header.getValue))
	// println(crossArea.getName +"  :  " + crossArea.getValue)
	println("------------CheckRequest Over-----------------------")
	
	
	// 获取验证码  获取验证码的 存在 Session中的东西   verifysession
	def veriCodeUrl =
		"https://ssl.captcha.qq.com/getimage?aid=522005705&"+
		s"r=${Math.random}" + "&uin=361541673@qq.com"
	def vericodeHeaders = 
	 	new BasicHeader("origURL", LOGINURLOFQQ) ::
	 	new BasicHeader("domain", "ssl.captcha.qq.com") ::
	 	new BasicHeader("Cookie", "ptisp=ctc") ::
	 	Nil
	val veriCodeRequest = sendRequest(vericodeHeaders, veriCodeUrl)
	// val  veriCodeResp = receiveResponse(veriCodeRequest)
/*	val verifysession = veriCodeResp.getHeaders("Set-Cookie").toList.map(header => 
		(header.getValue.split(";")(0)).split("=")(1)
	).headOption.getOrElse("")
*/
	println("------------veriCodeRequest Over-----------------------")
	
	// login?
	def loginHeaders(verifySession: String) =  
		new BasicHeader("origURL", LOGINURLOFQQ) ::
	 	new BasicHeader("domain", "ssl.ptlogin2.qq.com") ::
	 	new BasicHeader("Cookie", s"confirmuin=0; ptisp=ctc; verifysession=${verifySession}") ::
	 	Nil
	def loginUrl = {
		"https://ssl.ptlogin2.qq.com/login?"+
		"ptlang:2052&"+
		"aid:522005705&"+
		"daid:4&"+
		"u1:https://mail.qq.com/cgi-bin/login?vt=passport&vm=wpt&ft=ptlogin&ss=&validcnt=&clientaddr=361541673@qq.com&"+
		"from_ui:1&" +
		"ptredirect:1&" +
		"h:1&" +
		"wording:快速登录&" +
		"css:https://mail.qq.com/zh_CN/htmledition/style/fast_login181b91.css&" +
		"mibao_css:m_ptmail&" + 
		"u_domain:@qq.com&" +
		"uin:361541673&" +
		"u:361541673@qq.com&"+
		"p:340E254B4D3CC3ABB2F9EBECE3353B13&"+
		"verifycode:NMHP&"+
		"fp:loginerroralert&" +
		"action:2-26-16447&" +
		"g:1&" +
		"t:1&" +
		"js_type:2&" +
		"js_ver:10009"
	}

	// val loginRequest = sendRequest(loginHeaders(verifysession), loginUrl)
	// val loginResp = receiveResponse(loginRequest)
	// println ( loginResp.getHeaders("Set-Cookie").toList )
	
	println("------------loginRequest Over-----------------------")
	// check?uin 单击登录按钮时候的请求
	/*m.Login(
		m.clickAndVerify,
		"ssl.ptlogin2.qq.com",
		"https://ssl.ptlogin2.qq.com/login?ptlang=2052&aid=522005705&daid=4&u1=https%3A%2F%2Fmail.qq.com%2Fcgi-bin%2Flogin%3Fvt%3Dpassport%26vm%3Dwpt%26ft%3Dptlogin%26ss%3D%26validcnt%3D%26clientaddr%3D361541673%40qq.com&from_ui=1&ptredirect=1&h=1&wording=%E5%BF%AB%E9%80%9F%E7%99%BB%E5%BD%95&css=https://mail.qq.com/zh_CN/htmledition/style/fast_login181b91.css&mibao_css=m_ptmail&u_domain=@qq.com&uin=361541673&u=361541673@qq.com&p=026B2504DB3CEB95C590F007CCA05737&verifycode=!TTK&fp=loginerroralert&action=3-24-52872&g=1&t=1&dummy=&js_type=2&js_ver=10009"
	)*/
	
}	
