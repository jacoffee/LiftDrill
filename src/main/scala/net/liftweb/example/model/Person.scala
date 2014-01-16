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

	// transferorm string to calendar, usually the string is from input
	def stringToCal(dateLike: String) = {
		// date format authentication
		try {
			dateToCal(formatString(dateLike))
		} catch {
			case e: Exception => {
				val calendar = Calendar.getInstance
				calendar
			}
		}
	}
	def dateToCal(date: Date) = {
		val calendar = Calendar.getInstance
		calendar.setTime(date)
		calendar
	}

	def setCalendar = {
		val calendar = Calendar.getInstance
		calendar.set(2014, 4, 12, 14, 25, 12)
		calendar
	}
	def dfltUser = {
		Person.firstName("lao").
			lastName("luo").
			email("ad2121d@hotmail.com").
			birthDate(setCalendar).
			personalityType(Personality.rand).
			password("sd43434").save
	}

	def idValue = id.get.toString
	def deleteUser(oid: String) = delete(id.name, oid)
}

class Person extends MongoRecord[Person] with ObjectIdPk[Person] {
	def meta = Person
	object firstName extends StringField[Person](this, 100)
	object lastName extends StringField[Person](this, 100)
	object password extends StringField[Person](this, 30)
	object email extends StringField[Person](this, 100)
	object birthDate extends DateTimeField(this)
	object personalityType extends EnumField(this, Personality)
}

// extends JsonObjectFi eld[OwnerType, Password](rec, Password)
object Personality extends Enumeration {
	val TypeA = Value(1, "Type A")
	val TypeB = Value(2, "Type B")
	val ENTJ = Value(3, "ENTJ")
	val INTJ = Value(4, "INTJ")
	val allTypes = Array(TypeA, TypeB, ENTJ, INTJ)
	def rand = allTypes(Helpers.randomInt(allTypes.length))
}
