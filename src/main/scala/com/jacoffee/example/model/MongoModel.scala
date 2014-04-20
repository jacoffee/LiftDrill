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

	object created_at extends DateTimeField[ModelType](self) with LifecycleCallbacks {
		val fieldLabel ="创建时间"
		override def afterSave {
			this.set(Calendar.getInstance)
		}
	}

	object update_at extends DateTimeField[ModelType](self) with LifecycleCallbacks {
		val fieldLabel ="更新时间"

		override def afterSave {
			this.set(Calendar.getInstance)
		}
	}

}

// trait MongoMetaRecord[BaseRecord <: MongoRecord[BaseRecord]]
trait MongoModelMeta[ModelType <: MongoModel[ModelType]] extends MongoMetaRecord[ModelType] { self: ModelType =>
	def toObjectIdOption(idString: String) = if (ObjectId.isValid(idString)) Some(new ObjectId(idString)) else None
}
