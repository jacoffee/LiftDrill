package com.jacoffee.example.model

import org.bson.types.ObjectId
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import net.liftweb.mongodb.record.field.{MongoListField, ObjectIdPk}
import net.liftweb.record.Field
import net.liftweb.record.field.{ StringField => LiftStringField }



/**
 * Created by qbt-allen on 20114-4-19.
 */

trait LabelField {
	def fieldLabel: String
}

// (rec: OwnerType, val maxLength: Int)
abstract class StringField[OwnerType <: MongoModel[OwnerType]](rec: OwnerType, maxLength: Int) extends LiftStringField[OwnerType](rec, maxLength) with LabelField {}

object Article extends Article with MongoModelMeta[Article] {
	override def collectionName = "articles"

	def getFields = fields.flatMap {
		case field: StringField[Article] =>Some(field)
		case _ => None
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
