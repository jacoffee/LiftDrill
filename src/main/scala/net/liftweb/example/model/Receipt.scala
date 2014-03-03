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


// use Denormalization to speed up read
object Receipt extends Receipt with MongoMetaRecord[Receipt] {
	override val mongoIdentifier = MongoConfig.DefaultMongoIdentifier
	override def collectionName = "Receipt"
}

class Receipt extends MongoRecord[Receipt] with ObjectIdPk[Receipt] {
	def meta = Receipt
	object user extends StringField[Receipt](this, 100)
	object items extends MongoListField[Receipt, ObjectId](this)
	// productId2, productId3
}



