package com.jacoffee.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.io.{ FileOutputStream, File }
import org.apache.lucene.document.NumericField
import org.apache.lucene.document.Field.{ TermVector, Index, Store }
import org.apache.lucene.search.{ SortField, MatchAllDocsQuery }
import net.liftweb.record.field.{ StringField, IntField }
import net.liftweb.mongodb.record.field.MongoListField
import com.jacoffee.example.util.Config.UploadPath
// pay attention to the difference of seq2jvalue and list2jvalue
import net.liftweb.json.JsonDSL.{ pair2jvalue, string2jvalue, int2jvalue, seq2jvalue }

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
		println(" New Or Update Article!!")
		meta.indexOne(idValue)
		super.afterSave
	}

	override def afterDelete {
		println(" Delete Article!!")
		meta.indexOne(idValue)
		super.afterDelete
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

	object orderKinds extends Enumeration {
		val like = Value(1, "喜欢")
	}
	def getByTextSearch(queryText: String, params: Map[String, String], orderType: orderKinds.Value = orderKinds.like) = {
		getIndexModels(
			queryText,
			params,
			Array(
				author.name,
				title.name,
				content.name
			),
			orderType match {
				case orderKinds.like => Some(SortInfo(like.name, SortField.LONG, true))
			}
		)
	}

	def createFieldHighlighter(search: String, fieldToHighlight: String, textToDivide: String) = {
		highlightText(
			search,
			fieldToHighlight,
			Array(
				author.name,
				title.name,
				content.name
			),
			textToDivide
		)
	}
	def indexAll {
		println(" IndexALL ING")
		indexAll{ getAllDocuments._1.flatMap(doc => toObjectIdOption(doc.get(idIndexFieldName))).toList.distinct }
	}

	def saveImage(fileName: String, what: Array[Byte]) = {
		val path = UploadPath.getPublicPath(collectionName, fileName)
		val file = new File(path).getCanonicalFile
		if (!file.getParentFile.exists) file.getParentFile.mkdirs
		val outputStream = new FileOutputStream(file)
		outputStream.write(what)
		outputStream.close
	}

}
