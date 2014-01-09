package net.liftweb.example.snippet

import scala.xml.NodeSeq
import net.liftweb.mapper._
import net.liftweb.http.S._
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import net.liftweb.example.model.{Personality, Person}
import net.liftweb.util.BindPlus.nodeSeqToBindable
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.json.JsonAST.JObject
import net.liftweb.mongodb.Limit

/**
 * This snippet handles counting
 */
class Database {

  def render(in: NodeSeq): NodeSeq = {
   // val count = Person.count
    // 按照姓名的 姓氏 进行升序排列 
    // findAll(JObject(Nil), (Person.firstName.name, 1), Limit(2))
    // findAll(JObject(Nil), (created_at.name, -1), Limit(1))
    //val first = Person.findAll
    /*in.bind("database", 
      "count" -> count,
      "first" -> first.map(_.asHtml).openOr(<b>No Persons in the system</b>),
      "submit" -> submit("Create More Records", () => {
        val cnt = 10 + randomInt(50)
        for (x <- 1 to cnt) Person.create.firstName(randomString(20)).lastName(randomString(20)).personalityType(Personality.rand).save
        notice("Added " + cnt + " records to the Person table")
      }))*/
    Nil
  }
}
