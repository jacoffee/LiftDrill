package com.jacoffee.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import scala.io.{Codec, Source}
import scala.collection.JavaConversions.setAsJavaSet
import org.apache.lucene.document.{ Field, Document }
import org.apache.lucene.document.Field.{ TermVector, Index, Store }
import org.apache.lucene.index.{Term, IndexWriterConfig, IndexWriter}
import org.bson.types.ObjectId
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import net.liftweb.mongodb.record.field.{MongoListField, ObjectIdPk}
import net.liftweb.record.field.{ StringField => LiftStringField }
import org.apache.lucene.store.FSDirectory
import java.io.{StringReader, Reader, File}
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import org.apache.lucene.analysis.{WhitespaceAnalyzer, WordlistLoader, Analyzer}
import org.apache.lucene.analysis.tokenattributes.{TypeAttribute, OffsetAttribute, PositionIncrementAttribute, CharTermAttribute}
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.search.{TermQuery, IndexSearcher}
import org.apache.lucene.search.highlight._
import scala.Some

/**
 * Created by qbt-allen on 20114-4-19.
 */

abstract class StringField[OwnerType <: MongoModel[OwnerType]](rec: OwnerType, maxLength: Int) extends LiftStringField[OwnerType](rec, maxLength) {}

object Article extends Article with MongoModelMeta[Article] {
	override def collectionName = "articles"

	def getFields = fields.flatMap {
		case field: StringField[Article] =>Some(field)
		case _ => None
	}

	def getPublishDate(cal: Calendar) = {
		// 2014年09月10日 09:19
		val d = new SimpleDateFormat("yyyy年MM月dd日 HH:MM")
		d.format(cal.getTime)
	}

	val version = Version.LUCENE_34
	val indexedFilePosition = "D:/LiftDrill/src/main/resources/lucene/article"
	val analyzer = new SmartChineseAnalyzer(version, getStopWordsSet)
	def getStopWordsSet = {
		//Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("lucene/stopwords.txt"))(Codec.UTF8).getLines.toSet
		val stopWordSrc = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("lucene/stopwords.txt"))(Codec.UTF8)
		// println("Orginal Ones" +stopWordSrc.getLines.toList)
		try {
			WordlistLoader.getWordSet(classOf[Article], "/lucene/stopwords.txt", "//")
		} finally {
			stopWordSrc.close
		}
	}

	def indexArticle(article: Article) = {
		// 要养成关闭流的习惯 就像查询数据库一样敏感
		// where to save the index
		val directory = FSDirectory.open(new File(indexedFilePosition))
		val config = new IndexWriterConfig(version, analyzer)
		 val indexWriter = new IndexWriter(directory, config)

		val doc = new Document
		// idValue is ObjectId[]
		// if Index.NO is specified for a field,you must also specify TermVector.NO
		doc.add(new Field(article.id.name, article.idValue.toString, Store.YES, Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO))
		doc.add(new Field(article.author.name, article.author.get, Store.YES, Index.NOT_ANALYZED))
		doc.add(new Field(article.title.name, article.title.get, Store.YES, Index.ANALYZED))
		doc.add(new Field(article.content.name, article.content.get, Store.YES, Index.ANALYZED_NO_NORMS, Field.TermVector.YES))
		doc.add(new Field(article.comment.name, article.comment.get, Store.NO, Index.ANALYZED))

		indexWriter.addDocument(doc)
		try {
			indexWriter.close
		} finally {
			if (IndexWriter.isLocked(directory)) {
				IndexWriter.unlock(directory)
			}
		}
	}

	def search(fieldName: String,  searchString: String) = {
		// 获取命中文档ID
		val iSearch = new IndexSearcher(FSDirectory.open(new File(indexedFilePosition)))
		// val parser = new QueryParser(version, fieldName, new SmartChineseAnalyzer(version))
		val termQuery = new TermQuery(new Term("content", searchString))
		//val parsedQuery = parser.parse(searchString)
		val topDocs = iSearch.search(termQuery, 5)
		val objectIds =topDocs.scoreDocs.toList.map {hitDoc =>
			val actualDoc = iSearch.doc(hitDoc.doc)

			// 然后发现这种方式是走不通的 因为我根本就没有 Store Content Field 所以
			// Otherwise, the analyzer you pass in is used to reanalyze the text. in no way can happen
			// Field content in document is not stored and cannot be analyzed
/*			val tokenStream = TokenSources.getAnyTokenStream(iSearch.getIndexReader, hitDoc.doc, "content", actualDoc, analyzer)
			val term = tokenStream.addAttribute(classOf[CharTermAttribute])
			val analyzedContentList = Stream.continually((tokenStream.incrementToken, term.toString)).takeWhile(_._1).map(t =>s"[${t._2}]").toList
			println(" hit record's tokenStream ")
			println(analyzedContentList)*/
			val termVectorAndFreq = iSearch.getIndexReader.getTermFreqVector(hitDoc.doc, "content")
			// println(termVectorAndFreq.getTerms)
			// println(termVectorAndFreq)
			actualDoc.get(id.name)
		}
		// obtain tokenStream after indexing without setting TermVector
		// (IndexReader reader, int docId, String field, Document doc, Analyzer analyzer)
		// 根据ID 再次查询
		findAll( objectIds.flatMap{ oid => this.toObjectIdOption(oid) })
	}

	def highlightText(search: String, fieldName:String, textToDivide: String) = {
		// val tokenStream = TokenSources.getAnyTokenStream(iSearch.getIndexReader, hitDoc.doc, "content", actualDoc, analyzer)
		val termQuery = new TermQuery(new Term(fieldName, search))
		val scorer = new QueryScorer(termQuery, fieldName)
		val formatter = new SimpleHTMLFormatter("""<span class="highlight">""", """</span>""")

		val highlighter = new Highlighter(formatter, scorer)
		val tokenStream = analyzer.tokenStream("f", new StringReader(textToDivide))

		highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer))
		highlighter.setMaxDocCharsToAnalyze(1000)
		highlighter.getBestFragment(tokenStream, textToDivide)
	}

	def AnalyzerUtils(analyzer: Analyzer, reader: Reader) = {
		val stream = analyzer.reusableTokenStream("", reader)
		val term = stream.addAttribute(classOf[CharTermAttribute])
		Stream.continually((stream.incrementToken, term.toString)).takeWhile(_._1).map(t =>s"[${t._2}]")
	}

}

class Article extends MongoModel[Article] {
	def meta = Article
	object author extends StringField[Article](this, 50) {
		val fieldLabel = "作者"
	}
	object title extends StringField[Article](this, 100) {
		val fieldLabel = "标题"
	}
	object tags extends MongoListField[Article, String](this) {
		val fieldLabel = "标签"
	}
	object content extends StringField[Article](this, 2000) {
		val fieldLabel = "内容"
	}
	object comment extends StringField[Article](this, 100) {
		val fieldLabel = "评论"
	}
}
