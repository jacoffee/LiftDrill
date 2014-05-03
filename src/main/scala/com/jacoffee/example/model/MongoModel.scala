package com.jacoffee.example.model

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.DateTimeField
import net.liftweb.record.LifecycleCallbacks
import java.util.Calendar
import org.bson.types.ObjectId

/**
 * Created by qbt-allen on 14-4-19.
 */
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

	def invokeBeforeSave = meta.invokeBeforeSave(this)
	def invokeAfterSave = meta.invokeAfterSave(this)
}

// trait MongoMetaRecord[BaseRecord <: MongoRecord[BaseRecord]]
trait MongoModelMeta[ModelType <: MongoModel[ModelType]] extends MongoMetaRecord[ModelType] { self: ModelType =>
	def toObjectIdOption(idString: String) = if (ObjectId.isValid(idString)) Some(new ObjectId(idString)) else None

	def invokeBeforeSave(model: ModelType) = foreachCallback(model, _.beforeSave)
	def invokeAfterSave(model: ModelType) = foreachCallback(model, _.afterSave)
}
