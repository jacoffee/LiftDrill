package net.liftweb.example.DivideAndConquer

import java.net.URL
import java.io.{ InputStreamReader, BufferedReader }
import scala.io.Source

class YahooFinance {
	// http://finance.yahoo.com/echarts?s=CSV+Interactive
	// fetch the price of certain stock like BIDU/SINA/YaHOO
	def getPrice(ticker: String) = {
		val fromURL = s"http://ichart.finance.yahoo.com/table.csv?s=${ticker}&a=00&b=02&c=2014&d=02&e=04&f=&ignore=.csv"
		Source.fromURL(fromURL).getLines.foreach { println _}
	}
}

object fetchTicker extends App {
	val yf = new YahooFinance
	yf.getPrice("BIDU")
}