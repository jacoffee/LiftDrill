package com.jacoffee.example.model

import org.apache.lucene.document.{ Document, Field, Fieldable }
import org.apache.lucene.document.Field.{ TermVector, Index, Store }

/**
 * Created by qbt-allen on 14-6-6.
 */
trait IndexableModel[ModelType <: IndexableModel[ModelType]] extends MongoModel[ModelType] { self: ModelType =>
	val idIndexFieldName = idFieldName
	def idIndexFieldValue = idValue
	def fields: List[Fieldable]
	def index = {
		val document = new Document
		(
			new Field(idIndexFieldName, idIndexFieldValue.toString, Store.NO, Index.ANALYZED, TermVector.NO) :: fields
		).foldLeft(document)((doc, field) => {
				doc.add(field);document
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

trait IndexableModelMeta[ModelType <: IndexableModel[ModelType]] extends IndexableModel[ModelType] {
	self:ModelType =>

	def indexModel(model: ModelType) = model.index
}

