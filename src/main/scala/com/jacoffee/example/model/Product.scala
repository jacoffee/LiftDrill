package com.jacoffee.example.model

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import net.liftweb.record.field.DoubleField
import com.jacoffee.example.util.Config

// study database normailization && denormalization
object Product extends Product with MongoMetaRecord[Product] {
	override val mongoIdentifier = Config.Mongo.DefaultMongoIdentifier
	override def collectionName = "product"
}

class Product extends MongoRecord[Product] with ObjectIdPk[Product] {
	def meta = Product
	object name extends StringField[Product](this, 100)
	object price extends DoubleField[Product](this, 0.0)
	object desc extends StringField[Product](this, 500)
}



