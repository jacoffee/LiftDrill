package com.jacoffee.example.model

import org.bson.types.ObjectId
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import net.liftweb.example.MongoConfig
import net.liftweb.mongodb.record.field.{MongoListField, ObjectIdPk}
import net.liftweb.record.Field
import net.liftweb.record.field.StringField



/**
 * Created by qbt-allen on 20114-4-19.
 */
object Article extends Article with MongoModelMeta[Article] {
	override val mongoIdentifier = MongoConfig.DefaultMongoIdentifier
	override def collectionName = "articles"

	def getFields = fields.flatMap {
		case filed: Field => Some(filed)
		case _ => None
	}
}

class Article extends MongoModel[Article] {
	def meta = Article
	object author extends StringField[Article](this, 50)
	object title extends StringField[Article](this, 100)
	object tags extends MongoListField[Article, String](this)
	object content extends StringField[Article](this, 2000)
	object comment extends StringField[Article](this, 100)
}
