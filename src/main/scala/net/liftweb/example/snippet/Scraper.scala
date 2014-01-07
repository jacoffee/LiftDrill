package net.liftweb.example.snippet

import scala.collection.JavaConversions.asScalaBuffer
import net.liftweb.http.DispatchSnippet
import scala.xml.NodeSeq
import org.jsoup.Jsoup

object Scraper extends DispatchSnippet {
	def dispatch = {
		case "wiki"  =>  wiki
		case "htmlParser"  => htmlParser 
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
	
	def htmlParser(xhtml: NodeSeq): NodeSeq = {
		val html = "<html><head><title>First parse</title></head><body><p>Parsed HTML into a doc.</p></body></html>"
		val document = Jsoup.parse(html)
		<div>
			<p>String paser</p> ++
			{ document }
		</div>
	}
}





