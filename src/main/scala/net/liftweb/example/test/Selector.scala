package net.liftweb.example.test

import org.jsoup.Jsoup

object Selector extends App {
	val aa = """
		<form method="post" id="frm" name="frm" target="actionFrame" action="/cgi-bin/compose_send" enctype="multipart/form-data">
			<input type="text" name="password" placeholder="what's your password" autocomplete="off" />
			<input type="submit" value="GO" class="bb"/>	
		</form>
	"""
	val sendMailForm = Jsoup.parse(aa).select("form#frm").first
	sendMailForm.removeAttr("enctype").removeAttr("target").attr("action", "/tencent/mail")
	sendMailForm.select("input.bb").remove
	sendMailForm.append("huhu")
	println(sendMailForm)
}