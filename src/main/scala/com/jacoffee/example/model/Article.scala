package com.jacoffee.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.io.{ StringReader, Reader, File }
import scala.io.{ Codec, Source }
import scala.collection.JavaConversions.setAsJavaSet
import org.apache.lucene.document.{NumericField, Field, Document}
import org.apache.lucene.document.Field.{ TermVector, Index, Store }
import org.apache.lucene.index.{Term, IndexWriterConfig, IndexWriter}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import org.apache.lucene.analysis.{WhitespaceAnalyzer, WordlistLoader, Analyzer}
import org.apache.lucene.analysis.tokenattributes.{ TypeAttribute, OffsetAttribute, PositionIncrementAttribute, CharTermAttribute }
import org.apache.lucene.queryParser.{MultiFieldQueryParser, QueryParser}
import org.apache.lucene.search.{SortField, Sort, TermQuery, IndexSearcher}
import org.apache.lucene.search.highlight._
import net.liftweb.record.field.{ StringField, IntField }
import net.liftweb.mongodb.record.field.{MongoListField, ObjectIdPk}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import com.jacoffee.example.util.{ Helpers, Config }
import com.jacoffee.example.util.Config.Lucene.{ version, getIndexedFilePosition, getStopWordsSet, smartChineseAnalyzer }
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonDSL.{ pair2jvalue, string2jvalue, int2jvalue, seq2jvalue }
// pay attention to the difference of seq2jvalue and list2jvalue

/**
 * Created by qbt-allen on 20114-4-19.
 */

//abstract class StringField[OwnerType <: MongoModel[OwnerType]](rec: OwnerType, maxLength: Int) extends LiftStringField[OwnerType](rec, maxLength) {}

object Article extends Article with MongoModelMeta[Article] {
	override def collectionName = "articles"

	def getPublishDate(cal: Calendar) = {
		// 2014年09月10日 09:19
		val d = new SimpleDateFormat("yyyy年MM月dd日 HH:MM")
		d.format(cal.getTime)
	}

	val indexedFilePosition = getIndexedFilePosition("article")

	// class to build Sort and Query Info to better display Article to end-user
	// class QueryInfo()
	def search(fieldName: String, searchString: String) = {
		// 获取命中文档ID
		val iSearch = new IndexSearcher(FSDirectory.open(new File(indexedFilePosition)))
		val parser =  new MultiFieldQueryParser(version, Array(fieldName, title.name), smartChineseAnalyzer)
		val parsedQuery = parser.parse(searchString)
		// add sort field
		val sort = new Sort(new SortField(like.name,SortField.INT, true))
		val topDocs = iSearch.search(parsedQuery, 5, sort)
		val objectIds =topDocs.scoreDocs.toList.map { hitDoc =>
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
			println(" article likenum" +  actualDoc.get(like.name)) // 10 - 9 - 6
			actualDoc.get(id.name)
		}
		// obtain tokenStream after indexing without setting TermVector
		// (IndexReader reader, int docId, String field, Document doc, Analyzer analyzer)
		// 根据ID 再次查询
		// XXX Pay attention: this is will be ordered by ids so the lucene sort takes effect but overriden by DataBase order
		//  def findAll(qry: JObject, sort: JObject, opts: FindOption*): List[BaseRecord] =
		val ids = objectIds.flatMap{ oid => toObjectIdOption(oid) }.map(_.toString)
		findAll(idFieldName -> ("$in", ids), like.name -> -1)
	}

	def  indexArticle(article: Article) = {
		// 要养成关闭流的习惯 就像查询数据库一样敏感
		// where to save the index
		val directory = FSDirectory.open(new File(indexedFilePosition))
		val config = new IndexWriterConfig(version, smartChineseAnalyzer)
		val indexWriter = new IndexWriter(directory, config)

		val doc = new Document
		// idValue is ObjectId[]
		// if Index.NO is specified for a field,you must also specify TermVector.NO
		doc.add(new Field(article.id.name, article.idValue.toString, Store.YES, Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO))
		doc.add(new Field(article.author.name, article.author.get, Store.YES, Index.NOT_ANALYZED))
		doc.add(new Field(article.title.name, article.title.get, Store.YES, Index.ANALYZED))
		doc.add(new Field(article.content.name, article.content.get, Store.YES, Index.ANALYZED_NO_NORMS, Field.TermVector.YES))
		doc.add(new Field(article.comment.name, article.comment.get, Store.NO, Index.ANALYZED))
		// enhancement of original record  you can generate all kinds of  meta-records and put them in Lucene Index
		doc.add(new NumericField(article.like.name, Store.YES, true).setIntValue(article.like.get))

		indexWriter.addDocument(doc)
		try {
			indexWriter.close
		} finally {
			if (IndexWriter.isLocked(directory)) {
				IndexWriter.unlock(directory)
			}
		}
	}

	def highlightText(search: String, fieldName:String, textToDivide: String) = {
		// val tokenStream = TokenSources.getAnyTokenStream(iSearch.getIndexReader, hitDoc.doc, "content", actualDoc, analyzer)
		val parser = new MultiFieldQueryParser(version, Array(fieldName, "title"), smartChineseAnalyzer)
		val parsedQuery = parser.parse(search)
		val scorer = new QueryScorer(parsedQuery, null)
		val highlighter = new Highlighter(
			new SimpleHTMLFormatter("""<span class="highlight">""", """</span>"""),
			scorer
		)
		// String getBestFragment(Analyzer analyzer, String fieldName,String text)
		// 1000 stands for the size of byte that will be each hit
		def toOptional(text: String) = if (Helpers.isBlank(text)) None else Some(text)
		def geBestFragment(textToDivide: String) = toOptional(highlighter.getBestFragment(smartChineseAnalyzer, fieldName, textToDivide))
		highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer,1000))
		//  @return highlighted text fragment or null if no terms found so you have another consideration
		geBestFragment(textToDivide)
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
	object like extends IntField[Article](this) {
		override val defaultValue = 0
	}
	override def afterSave {
		println(" New Or Update Job!!")
		meta.indexArticle(this)
		super.afterSave
	}
}
