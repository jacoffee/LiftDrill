package net.liftweb.example.test

import org.jsoup.Jsoup
import scala.xml.parsing.XhtmlParser
import scala.io.Source
import org.jsoup.Connection.Response
import java.io.FileOutputStream
import net.liftweb.util.BasicTypesHelpers.asInt
import net.liftweb.http.RequestVar
object Selector extends App {
/*		def getErrorType(url: String) = {
			asInt(url.split("&").toList.filter(_.contains("errtype")).headOption.getOrElse("errtype=3").split("=").toList.last).getOrElse("3")
		}
		val url = """http://w.mail.qq.com/cgi-bin/loginpage?f=xhtmlmp&aliastype=@qq.com&s=&errtype=3&clientuin=1253246958&verify=&aliastype=@qq.com&fun=&g_key=&msg=&mss=1&autologin=n&plain=&ppp=&spcache=&3g_sid=&mtk="""
		// println(Jsoup.connect(html).execute.parse)
		val jumpto = """1;url=http://w.mail.qq.com/cgi-bin/loginpage?f=xhtmlmp&amp;errtype=3&amp;verify=true&amp;clientuin=372242999&amp;t=&amp;alias=&amp;regalias=&amp;aliastype=@qq.com&amp;autologin=n&amp;spcache=&amp;folderid=&amp;3g_sid=&amp;g_key=&amp;msg=&amp;ppp=eRc%3D&amp;autologin=n&amp;mss=1&amp;vurl=http://vc.gtimg.com/GP6MUT6HW51AYDKBSJ7QD79FSYYYY6NY&amp;vid=GP6MUT6HW51AYDKBSJ7QD79FSYYYY6NY&amp;vuin=hBXZrb1oAqYL7kCL2f-6zt-noPNPaomfA0divqaV-Qg.&amp;tfcont=22%20serialization%3A%3Aarchive%205%200%200%209%200%200%200%208%20authtype%201%208%209%20clientuin%209%20372242999%209%20aliastype%207%20%40qq.com%206%20domain%206%20qq.com%201%20f%205%20xhtml%203%20uin%209%20372242999%203%20mss%201%201%207%20btlogin%204%20%E7%99%BB%E5%BD%95%205%20https%204%20true&amp;authtype=8"""
		val pattern = """(url)=(http)""".r
		val bb = pattern  findFirstMatchIn jumpto match {
			case Some(gotcha) => gotcha.group(2)
			case  _ => "as"
		}
		println(bb)
		val cc = List(("", ""))
		val dd = cc.map {
			case (a, b) => println(a +" :" + b)
			case _ => println("什么都没有")
		}
		println(dd)
		val jump = "http://w.mail.qq.com/cgi-bin/loginpage?f=xhtmlmp&errtype=3&verify=true&clientuin=361541673&t=&alias=&regalias=&aliastype=@qq.com&autologin=n&spcache=&folderid=&3g_sid=&g_key=&msg=&ppp=bm8rYWwwYCtZ&autologin=n&mss=0&vurl=http://vc.gtimg.com/ST6MUT6HWM4ZYDKBSJ71NA1TIYYYY15K&vid=ST6MUT6HWM4ZYDKBSJ71NA1TIYYYY15K&vuin=LfGw0TDVXoqKADxvkv9eby5n-dAa8cJy5nPjeOT-Lp8.&tfcont=22%20serialization%3A%3Aarchive%205%200%200%208%200%200%200%208%20authtype%201%208%209%20clientuin%209%20361541673%209%20aliastype%207%20%40qq.com%206%20domain%206%20qq.com%2019%20F260814865311NXDUZP%204%20true%201%20f%205%20xhtml%205%20https%204%20true%203%20uin%209%20361541673&authtype=8"
	//	println( Jsoup.connect(jump).execute )
		
		(1 to 30000).toList.zipWithIndex map {
			case (elem, num) => { 
				val resp = Jsoup.connect("http://xyzp.haitou.cc/").execute
				if( num == 30000 ) {
					println(resp.body)
				}
			}
		}*/
		object errType extends RequestVar(Some("2"))
		val errTypeOfVerifycode = Map("2" -> "验证码错误", "3" -> "请输入验证码")
		val hh = if (errType.is.exists( err => errTypeOfVerifycode.get(err).nonEmpty) ) {
			"好的"
		} else {
			"坏的"
		}
		println("-------------" + hh)
		
		
}