package net.liftweb.example.model

import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.BsonMetaRecord
import net.liftweb.record.field.StringField
import net.liftweb.record.field.DoubleField


/*

Specialized MetaRecord that deals with BsonRecords 
trait BsonMetaRecord[BaseRecord <: BsonRecord[BaseRecord]] extends MetaRecord[BaseRecord] with JsonFormats {
  self: BaseRecord =>
  
  从上面的定义可以看出 混入它的话  BsonMetaRecord 必须首先是 BaseRecord的子类
  因为Fruit extends Fruit 所以 Object Fruit就相当于是 BsonRecord 的子类
  
 */
/*object Fruit extends Fruit with BsonMetaRecord[Fruit]

class Fruit extends BsonRecord[Fruit] { // type parameter是强类型的一个很好的体现
	def meta = Fruit  // def meta: BsonMetaRecord[MyType]
	object name extends StringField(this, 100)
	object price extends DoubleField(this) {
		override def defaultValue = 15.00
	}
	object origin extends StringField(this, 100)
}*/

