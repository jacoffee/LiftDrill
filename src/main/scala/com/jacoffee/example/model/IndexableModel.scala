package com.jacoffee.example.model

import java.io.File
import org.apache.lucene.document.{ Document, Field, Fieldable }
import org.apache.lucene.document.Field.{ TermVector, Index, Store }
import org.apache.lucene.search.{ Sort, SortField, Query, IndexSearcher }
import org.apache.lucene.index.IndexReader
import org.apache.lucene.store.FSDirectory
import com.jacoffee.example.util.Config.Lucene.{ version, getIndexedFilePosition, getStopWordsSet, smartChineseAnalyzer }

/**
 * Created by qbt-allen on 14-6-6.
 */
trait IndexableModel[ModelType <: IndexableModel[ModelType]] extends MongoModel[ModelType] { self: ModelType =>
	val idIndexFieldName = idFieldName
	def idIndexFieldValue = idValue
	def indexFields: List[Fieldable]
	def index = {
		val document = new Document
		(
			new Field(idIndexFieldName, idIndexFieldValue.toString, Store.NO, Index.ANALYZED, TermVector.NO) :: indexFields
		).foldLeft(document)(
			(doc, field) => {
				doc.add(field)
				doc
			}
		)
	}
	protected def getIndexFields(fieldName: String, fieldValue: String,
		store: Store, index: Index, termVector: TermVector,boostOption: Option[Float]) = {
		val field = new Field(idFieldName, fieldValue, store, index, termVector)
		boostOption.foreach(field.setBoost)
		field
	}
	def getNoIndexFields(fieldName: String, fieldValue: String) = {
		new Field(idFieldName, fieldValue, Store.NO, Index.NOT_ANALYZED_NO_NORMS, TermVector.NO)
	}
}

trait IndexableModelMeta[ModelType <: IndexableModel[ModelType]] extends IndexableModel[ModelType]
	with LuceneUtil {
	self:ModelType =>

	def indexModel(model: ModelType) = model.index

	// Update Lucene Index

	/* Search Lucene Index */
	/* Build Query Info
	* Pay attention to the Constructor
	* */
	case class SortInfo(fieldName: String, sortType: Int = SortField.LONG, reverse: Boolean= false)
	class QueryInfo(query: Query, sortOption: Option[Sort]) {
		// Also, each constructor must call one of the previously defined constructors.
		// one arg
		def this(query: Query, sort: Sort) = this(query, Some(sort))
		def this(query: Query) = this(query, None)
		def this(query: Query, sortInfo: SortInfo) =
			this(
				query,
				new Sort(new SortField(sortInfo.fieldName, sortInfo.sortType, sortInfo.reverse))
			)
	}


}

trait LuceneUtil {

	val cachedPeriod = 1000L * 60
	protected val collectionName: String
	val indexedFilePosition = getIndexedFilePosition(collectionName)
	val directory = FSDirectory.open(new File(indexedFilePosition))

	@volatile var cachedIndexSearcher: (Long, IndexSearcher) = {
		if ( System.currentTimeMillis - cachedIndexSearcher._1> cachedPeriod) {
			(System.currentTimeMillis, new IndexSearcher(IndexReader.open(directory, true)))
		} else {
			cachedIndexSearcher
		}
	}
}
