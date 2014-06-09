package com.jacoffee.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.io.{ StringReader, Reader, File }
import scala.io.{ Codec, Source }
import scala.collection.JavaConversions.setAsJavaSet
import org.apache.lucene.document.{NumericField, Field, Document}
import org.apache.lucene.document.Field.{ TermVector, Index, Store }
import org.apache.lucene.index.{IndexReader, Term, IndexWriterConfig, IndexWriter}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import org.apache.lucene.analysis.{WhitespaceAnalyzer, WordlistLoader, Analyzer}
import org.apache.lucene.analysis.tokenattributes.{ TypeAttribute, OffsetAttribute, PositionIncrementAttribute, CharTermAttribute }
import org.apache.lucene.queryParser.{MultiFieldQueryParser, QueryParser}
import org.apache.lucene.search.{MatchAllDocsQuery, SortField, Sort, TermQuery, IndexSearcher}
import org.apache.lucene.search.highlight._
import net.liftweb.record.field.{ StringField, IntField }
import net.liftweb.mongodb.record.field.{MongoListField, ObjectIdPk}
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import net.liftweb.json.JsonAST.JObject
// pay attention to the difference of seq2jvalue and list2jvalue
import net.liftweb.json.JsonDSL.{ pair2jvalue, string2jvalue, int2jvalue, seq2jvalue }
import org.bson.types.ObjectId
import com.jacoffee.example.util.{ Helpers, Config }
import com.jacoffee.example.util.Config.Lucene.{ version, getIndexedFilePosition, getStopWordsSet, smartChineseAnalyzer }
/**
 * Created by qbt-allen on 20114-4-19.
 */
//abstract class StringField[OwnerType <: MongoModel[OwnerType]](rec: OwnerType, maxLength: Int) extends LiftStringField[OwnerType](rec, maxLength) {}
class Article extends IndexableModel[Article] {
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

	def indexFields = {
		getIndexFields(author.name, author.get, Store.YES, Index.NOT_ANALYZED, TermVector.NO, None) ::
		getIndexFields(title.name, title.get, Store.YES, Index.ANALYZED, TermVector.NO, None) ::
		getIndexFields(content.name, content.get, Store.NO, Index.ANALYZED, TermVector.NO, Some(2f)) ::
		getNotIndexFields(comment.name, comment.get) ::
		new NumericField(like.name, Store.YES, true).setLongValue(like.get) ::
		Nil
	}

	override def afterSave {
		println(" New Or Update Job!!")
		meta.indexOne(this)
		super.afterSave
	}
}

object Article extends Article with IndexableModelMeta[Article] {
	override def collectionName = "articles"
	override val indexName = collectionName
	def getPublishDate(cal: Calendar) = {
		// 2014年09月10日 09:19
		val d = new SimpleDateFormat("yyyy年MM月dd日 HH:MM")
		d.format(cal.getTime)
	}

	// def getIndexModels(queryText: String, query: Query, fieldNameArray: Array[String], sortInfoOption: Option[SortInfo] = None) = {
	implicit def map2Query(params: Map[String, String]) = {
		if (params.isEmpty) new MatchAllDocsQuery
		else {
			// FIXME
			new MatchAllDocsQuery
		}
	}
	def getByTextSearch(queryText: String, params: Map[String, String], sortInfoOption: Option[SortInfo] = None) = {
		getIndexModels(
			queryText,
			params,
			Array(
				author.name,
				title.name,
				content.name
			),
			sortInfoOption
		)
	}

	def indexAll {
		val oidsFromDoc = getAllDocuments._1.flatMap(doc => toObjectIdOption(doc.get(idIndexFieldName))).toList
		indexAll(oidsFromDoc)
	}

}
