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
import net.liftweb.mongodb.MongoDB
import com.mongodb.gridfs.GridFS
import net.liftweb.mongodb.DefaultMongoIdentifier
import java.util.TreeMap


object Person extends Person with MongoMetaRecord[Person] {
	//override val mongoIdentifier = MongoConfig.DefaultMongoIdentifier
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
		calendar.set(2015, 3, 12, 6, 25, 12)
		calendar
	}
	def dfltUser = {
		val personModel = Person.createRecord
		println(" id  " + personModel.id.get)
		personModel.firstName("changshi").
			lastName("zhou yan").
			email("361541163@121.com").
			birthDate(setCalendar).
			personalityType(Personality.rand).
			password("123456789").save
/*		MongoDB.use(DefaultMongoIdentifier) { db =>
			  val fs = new GridFS(db)
			  val inputFile = fs.createFile(new java.io.File("E:/LiftDrill/src/main/webapp/bigdata/Account.scala"))
			  inputFile.setId(personModel.id.get)
			  inputFile.setContentType("text/plain")
			  inputFile.setFilename("xishi")
			  inputFile.save
		}*/

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
