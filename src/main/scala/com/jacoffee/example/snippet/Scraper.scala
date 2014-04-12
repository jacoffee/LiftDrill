package  com.jacoffee.example.snippet

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



object Scraper extends DispatchSnippet with Loggable {
	def dispatch = {
		case "stringToHtml"  => stringToHtml 
		case "extractAttr"  => extractAttr
		case "htCity" => htCity
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
}
