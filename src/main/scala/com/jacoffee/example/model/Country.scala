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



// country 中有一个字段flag  flag 也是一个document 现在想要把它嵌套在Country
// 此处的Country相当于以前的dao层  所以可以决定哪个 数据库去存储数据
object Country extends Country with MongoMetaRecord[Country] {
	override def collectionName = "example.earth"
}

class Country extends MongoRecord[Country] with ObjectIdPk[Country] {
	def meta = Country
	object flag extends BsonRecordField(this, Image) { // BsonRecordField used to embed document in existing 
		override def defaultValue = Image.createRecord.url("http://www.google.com").width(200).height(400)
	}
	object nation extends BsonRecordField(this, Image) {
		override def optional_? = true
		// write in this way we can use like this in Lift val img: Box[Resume] = Country.flag.valueBox
		// In fact, regardless of the setting of  optional_?, you can access the value using  valueBox.
	}
}



class Image extends BsonRecord[Image] {
	def meta = Image
	object url extends StringField(this, 1000)
	object width extends IntField(this)
	object height extends IntField(this)
}

object Image extends Image with BsonMetaRecord[Image] 

object In_Flag extends App {
	val server = new ServerAddress("127.0.0.1", 27017)
	MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(server), "LiftDrill")
	val unionFlag = Image.createRecord.url("http://www.baidu.com").width(100).height(300)
	Country.createRecord.save
	val img: Box[Image] = Country.flag.valueBox
}

