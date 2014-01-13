package net.liftweb.example.model

import net.liftweb.mongodb.record.{ MongoRecord, MongoMetaRecord }
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.{ StringField,  EnumField }
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.mongodb.record.field.MongoPasswordField
import net.liftweb.mongodb.record.field.Password
import net.liftweb.record.field.DateTimeField
import net.liftweb.example.MongoConfig
import net.liftweb.util.Helpers
import java.util.{ Calendar, Date }
import java.text.SimpleDateFormat


object Person extends Person with MongoMetaRecord[Person] {
	override val mongoIdentifier = MongoConfig.DefaultMongoIdentifier
	// sort accroding firstname
	def getAllSortByFirstName = findAll(JObject(Nil),(firstName.name, 1) )

	def numOfPeople = count

	// list all fields Name
	def listFieldsName = List(firstName, lastName, email, birthDate, personalityType).map(_.name)
	def getPersonAttrs(person: Person) =  List(firstName, lastName, email, birthDate, personalityType)

	def formatPattern = new SimpleDateFormat("yyyy-MM-dd HH:mm")
	def formatDate(date: Date) = formatPattern.format(date)
	def formatString(dateLike: String) = formatPattern.parse(dateLike)
	def dateToCal(date: Date) = {
		val calendar = Calendar.getInstance
		calendar.setTime(date)
		calendar
	}

	def setCalendar = {
		val calendar = Calendar.getInstance
		calendar.set(2012, 1, 10, 12, 25, 12)
		calendar
	}
	def dfltUser = {
		Person.firstName("xiao").
			lastName("ming").
			email("123456@163.com").
			birthDate(Calendar.getInstance).
			personalityType(Personality.rand).
			password(Password(""))
	}

	def idValue = id.get.toString

	def deleteUser(oid: String) = delete(id.name, oid)

}

class Person extends MongoRecord[Person] with ObjectIdPk[Person] {
	def meta = Person
	object firstName extends StringField[Person](this, 100)
	object lastName extends StringField[Person](this, 100)
	object password extends MongoPasswordField[Person](this, 30)
	object email extends StringField[Person](this, 100)
	object birthDate extends DateTimeField(this)
	object personalityType extends EnumField(this, Personality)
}

// extends JsonObjectField[OwnerType, Password](rec, Password)
object Personality extends Enumeration {
	val TypeA = Value(1, "Type A")
	val TypeB = Value(2, "Type B")
	val ENTJ = Value(3, "ENTJ")
	val INTJ = Value(4, "INTJ")
	val allTypes = Array(TypeA, TypeB, ENTJ, INTJ)
	def rand = allTypes(Helpers.randomInt(allTypes.length))
}
