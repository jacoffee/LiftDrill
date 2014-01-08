package net.liftweb.example.snippet

import scala.collection.JavaConversions.asScalaBuffer
import net.liftweb.http.DispatchSnippet
import scala.xml.NodeSeq
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Scraper extends DispatchSnippet {
	def dispatch = {
		case "wiki"  =>  wiki
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
		getCityNamesAndUrls(buildConnection(baseUrlOfXJH),cityLoc).map { 
			case (cityText, cityUrl) => {
				<div>
					<a href={ cityUrl } target="_blank" class="city">{ cityText }</a>
					{
						getUniversityInCity(cityUrl).map { case (universityText, universityUrl) =>
							<a href={ universityUrl } target="_blank">{ universityText }</a> 
						}
					}
				</div>
			} 
		}
	}
}





