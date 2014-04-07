package net.liftweb.example.model

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.{ StringField,  EnumField }
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.record.field.DateTimeField
import net.liftweb.example.MongoConfig
import net.liftweb.util.Helpers
import java.util.{ Calendar, Date }
import java.text.SimpleDateFormat
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.BsonMetaRecord
import net.liftweb.record.field.IntField
import net.liftweb.mongodb.record.field.BsonRecordField
import bootstrap.liftweb.Boot
import com.mongodb.ServerAddress
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.DefaultMongoIdentifier
import com.mongodb.Mongo
import net.liftweb.common.Box
import net.liftweb.record.field.DoubleField


// study database normailization && denormalization
object Product extends Product with MongoMetaRecord[Product] {
	override val mongoIdentifier = MongoConfig.DefaultMongoIdentifier
	override def collectionName = "product"
}

class Product extends MongoRecord[Product] with ObjectIdPk[Product] {
	def meta = Product
	object name extends StringField[Product](this, 100)
	object price extends DoubleField[Product](this, 0.0)
	object desc extends StringField[Product](this, 500)
}



