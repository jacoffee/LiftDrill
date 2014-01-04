package net.liftweb.example.record

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.MongoId
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import net.liftweb.record.field.IntField
import net.liftweb.mongodb.record.field.UUIDPk
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.JsonObject
import net.liftweb.mongodb.JsonObjectMeta
import net.liftweb.mongodb.record.field.JsonObjectField
import net.liftweb.mongodb.record.field.MongoJsonObjectListField
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JObject

// create book record  ---  rows  and save
object Book extends Book  with MongoMetaRecord[Book] {
		// 存储一条记录  
       	Config.initConnection
       	// def collectioName = "db"
       	//怎么同时存储三条记录
}


// 代表    ObjectId主键的属性 ObjectIdPk//  还有StringPK
class Book extends MongoRecord[Book] with ObjectIdPk[Book] { 

			// MongoRecord must have _id field  
			//def id:Any = ??? 如果不想在这个地方重写的话 就需要混入特质  《声明 ObjectId 字段》  
			def meta  = Book
			
			// 这个声明的过程  就像以前的Mapper 文件  将你要存入MongoDB的字段的类型声明出来
			object  title  extends StringField(this,100)
			object publishyear extends IntField(this,1998)
			
			// 多对一的 关系  出版社
			// Json对象的属性
// This  inner  object  extends JsonObjectField,  which  essentially  means  it  holds  a  nested  Mongo  document.  
// publisher对象 继承了 JsonObject 抽象类 说明它是Book的文档的内嵌文档

			// JsonObjectField  extends  trait MandatoryTypedField   def defaultValue: MyType
			object  publisher extends JsonObjectField[Book, Publisher](this,Publisher) {
					def defaultValue =  Publisher("", "")
			}
			
			//  1对多的关系   书本和作者的关系
			// 说明该属性 是一个  集合
			//  As  you  might  imagine,  this  contains  a  list  of  documents,  as
			//  opposed to the single document required by publisher			
			object authors extends MongoJsonObjectListField[Book, Author](this, Author) 
}


// 书籍

// class JsonObjectMeta[BaseDocument](implicit mf: Manifest[BaseDocument]) {
object Author extends JsonObjectMeta[Author]
case class Author(firstName: String, lastName: String) extends JsonObject[Author]{
		def meta = Author
}
// 上面的定义  可以这样理解  定义了一个张表 -- Author;  行的基本元素  Author


// 出版社
object Publisher extends JsonObjectMeta[Publisher]
case class Publisher(name: String, description: String) extends JsonObject[Publisher]{
		def meta = Publisher
}

object  Query {
	
		//  查询2008年之前的书籍
	  	//  private[this] val booksBefore2008: net.liftweb.common.Box[net.liftweb.example.record.Book]
		val booksBefore2008 = Book.findAll("publishyear" -> ("$gt" -> 2008))
		booksBefore2008.flatMap(
		    book => List ("Name:   "+ book.title + " Publisher " +book.publishyear)
		 ).foreach(println _)
}

object ComplexQuery extends App {
		
		// 存储一条 复杂的记录
//		Book.createRecord.title("Lift In Action").publishyear(2011).
//		authors(   List( Author("allen","chou"), Author("Tim","Cook")) ).
//		publisher(Publisher("Manning","the Best Publisher in the world")).save
  
	  	// 查询出所有的书籍
		//println (Book.findAll)
		val authorList  = Book.findAll.map(
				book => book.authors
		)		
		// 名字为 Lift In Action 的书的作者的FirstName    尚不可知？？
		
    	//	{"comments" : {"$elemMatch" : {"author" : "joe", "score" : {"$gte" : 5}}}})
		
		// println(authorList)
		val lia = Book.find("title" ->"Lift In Action")
		val lia1 = Book.find("title","Lift In Action")
		val jfield1  = new JField("title","Lift In Action")
		// db.books.find({"authors":{"$elemMatch":{}}},{"authors.firstName":1,"_id":0} )
		val lia2 = Book.find(new JObject(List(jfield1)))
		println(lia)
		println(lia1)
		println(lia2)
		
}
/*
        {
            "firstName" : "Tim",
            "lastName" : "Cook"
        }
    ],
    "publisher" : {
        "name" : "Manning",
        "description" : "the Best Publisher in the world"
    },
    "title" : "Lift In Action"
}
*/