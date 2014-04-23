package com.jacoffee.example.model

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.JsonObject
import net.liftweb.mongodb.JsonObjectMeta
import net.liftweb.mongodb.record.field.MongoJsonObjectListField
import com.jacoffee.example.util.Config

// use Denormalization to speed up read
object Food extends Food with MongoMetaRecord[Food] {
	override val mongoIdentifier = Config.Mongo.DefaultMongoIdentifier
	override def collectionName = "Food"
}

class Food extends MongoRecord[Food] with ObjectIdPk[Food] {
	def meta = Food
	object category extends StringField[Food](Food.this, 100)
	object sub extends MongoJsonObjectListField(this, Fruit)
}

object Fruit extends JsonObjectMeta[Fruit] {
	import net.liftweb.json.Formats
	import net.liftweb.json.JsonAST.{ JObject, JString }
	implicit val formats = net.liftweb.json.DefaultFormats
	override def create(in: JObject)(implicit formats: Formats): Fruit = {
		def getField(fieldName: String) = (in \ fieldName) match {
			case JString(string) => string
			case jValue => jValue.extractOrElse("")
		}
		Fruit(
			getField("name"),
			getField("price"),
			getField("origin")
		)
	}
}
case class Fruit(name: String, price: String, origin: String) extends JsonObject[Fruit] {
	def meta = Fruit  // def meta: BsonMetaRecord[MyType]
}
