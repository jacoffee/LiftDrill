package com.jacoffee.example.model

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.DateTimeField
import net.liftweb.record.LifecycleCallbacks
import net.liftweb.json.JsonAST.{JObject, JValue}
import net.liftweb.json.JsonDSL.{ pair2jvalue, string2jvalue, seq2jvalue, list2jvalue }
import java.util.Calendar
import org.bson.types.ObjectId

// trait MongoRecord[MyType <: MongoRecord[MyType]] extends BsonRecord[MyType]
trait MongoModel[ModelType <: MongoModel[ModelType]] extends MongoRecord[ModelType]  with ObjectIdPk[ModelType] { self: ModelType =>
	def meta: MongoModelMeta[ModelType]
	def idValue = id.get
	def idFieldName = id.name

	object created_at extends DateTimeField[ModelType](self) {
		val fieldLabel ="创建时间"
	}

	object update_at extends DateTimeField[ModelType](self) with LifecycleCallbacks {
		val fieldLabel ="更新时间"

		override def beforeSave {
			super.beforeSave
			set(Calendar.getInstance)
		}

		override def afterSave {
			super.afterSave
			// the trick is here, cause LiftcycleCallbacks is defined on field level
			// you have to no chance to use it on Record Curd Operation
			// but you could mingle something you wanna execute after record save into the field save
			// def owner = rec  here owner refers to the self  namely the Record Model Type
			owner.afterSave
		}
	}

	def afterSave {}

	// def invokeBeforeSave = meta.invokeBeforeSave(this)
	// def invokeAfterSave = meta.invokeAfterSave(this)
}

// trait MongoMetaRecord[BaseRecord <: MongoRecord[BaseRecord]]
trait MongoModelMeta[ModelType <: MongoModel[ModelType]] extends MongoModel[ModelType] with MongoMetaRecord[ModelType] { self: ModelType =>
	def toObjectIdOption(idString: String) = if (ObjectId.isValid(idString)) Some(new ObjectId(idString)) else None
	implicit def objectIdToString(id: ObjectId) = id.toString
	implicit def objectIdsToListString(ids: List[ObjectId])= ids.map(objectIdToString)

	// def invokeBeforeSave(model: ModelType) = foreachCallback(model, _.beforeSave)
	// def invokeAfterSave(model: ModelType) = foreachCallback(model, _.afterSave)

	def getBoxById(id: ObjectId) = find(idFieldName -> id.toString)
	def findIn(ids: List[ObjectId]) = findAll(id.name -> ("$in" -> objectIdsToListString(ids)))
}
