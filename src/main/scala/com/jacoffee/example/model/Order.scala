package com.jacoffee.example.model

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.field.MongoListField
import org.bson.types.ObjectId
import com.jacoffee.example.util.Config

object Order extends Order with MongoMetaRecord[Order] {
	override val mongoIdentifier = Config.Mongo.DefaultMongoIdentifier
	override def collectionName = "order"
}

class Order extends MongoRecord[Order] with ObjectIdPk[Order] {
	def meta = Order
	object user extends StringField[Order](this, 100)
	object items extends MongoListField[Order, ObjectId](this)
	// productId2, productId3
}



