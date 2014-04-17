package com.jacoffee.example.Lucene

import java.io.{StringReader, Reader}
import scala.io.Source
import org.apache.lucene.analysis.{ Analyzer, StopAnalyzer, SimpleAnalyzer, WhitespaceAnalyzer }
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

/**
 * Created by qbt-allen on 14-4-17.
 */
// use this simple example to see the visual effect of all kinds of Analyzed Norms
object  AnalyzerDemo extends App  {
	val version = Version.LUCENE_34
	val textToAnalyzed = Array("The quick brown fox 1jumped 2over the lazy dog", "XY&Z Corporation34 - xyz@example.com")

	val stopWordsSet = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("props/stopwords.txt")).toSet
	val possibleAnalyzers = {
		List(
			new WhitespaceAnalyzer(version),
			new SimpleAnalyzer(version),
			new StopAnalyzer(version),
			new StandardAnalyzer(version)
		)
	}

	// AnalyzerUtils.displayTokens(analyzer, text);
	possibleAnalyzers.foreach { analyzer =>
		println(" Analyzer: " + analyzer.getClass.getSimpleName)
		println(" -------------- Display Tokens --------------")
		textToAnalyzed.foreach { text =>
			println(" Parse Text " + text)
			val tokens = AnalyzerUtils(analyzer, new StringReader(text))
			println(" Tokens Produced " + tokens)
		}
		println(" &&&&&&&&&&&&&&&&&&&&&&& ")
		println(" &&&&&&&&&&&&&&&&&&&&&&& ")
		println("")
	}

	def AnalyzerUtils(analyzer: Analyzer, reader: Reader) = {
		val stream = analyzer.reusableTokenStream("", reader)
		val term = stream.addAttribute(classOf[CharTermAttribute])
		Stream.continually((stream.incrementToken, term.toString)).takeWhile(_._1).map(t =>s"[${t._2}]").toList
	}
}
