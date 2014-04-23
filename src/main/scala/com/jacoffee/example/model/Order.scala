package net.liftweb.example.model

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.{ StringField,  EnumField, DateTimeField }
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.example.MongoConfig
import net.liftweb.util.Helpers
import java.util.{ Calendar, Date }
import java.text.SimpleDateFormat
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.BsonMetaRecord
import net.liftweb.record.field.IntField
import net.liftweb.mongodb.record.field.BsonRecordField
import net.liftweb.mongodb.DefaultMongoIdentifier
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



