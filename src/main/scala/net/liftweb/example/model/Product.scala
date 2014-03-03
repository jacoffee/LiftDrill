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


// study database normailization && denormalization
object Product extends Product with MongoMetaRecord[Product] {
	override val mongoIdentifier = MongoConfig.DefaultMongoIdentifier
	override def collectionName = "Product"
}

class Product extends MongoRecord[Product] with ObjectIdPk[Product] {
	def meta = Product
	object flag extends BsonRecordField(this, Image) { // BsonRecordField used to embed document in existing 
		override def defaultValue = Image.createRecord.url("http://www.google.com").width(200).height(400)
	}
	object nation extends BsonRecordField(this, Image) {
		override def optional_? = true
		// write in this way we can use like this in Lift val img: Box[Resume] = Product.flag.valueBox
		// In fact, regardless of the setting of  optional_?, you can access the value using  valueBox.
	}
}



