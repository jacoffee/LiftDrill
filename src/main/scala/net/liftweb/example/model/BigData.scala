package net.liftweb.example.model

import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.DefaultMongoIdentifier
import com.mongodb.gridfs.GridFS
import com.mongodb.Mongo
import com.mongodb.ServerAddress
import org.bson.types.ObjectId

object BigData  extends App {
  	val server = new ServerAddress("localhost",27017)
	MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(server), "LiftDrill")
	
	MongoDB.use(DefaultMongoIdentifier) { db =>
		  val fs = new GridFS(db)
		  val inputFile = fs.createFile(new java.io.File("E:/LiftDrill/src/main/webapp/bigdata/Account.scala"))
		  inputFile.setContentType("text/plain")
		  inputFile.setFilename("sifang")
		  inputFile.setId(new ObjectId("5325191873f9f274a7f330ad"))
		  inputFile.save
	}
  	
/*  MongoDB.use(DefaultMongoIdentifier) { db =>
  		val fs = new GridFS(db)
  		val foundFile = fs.findOne("sifang")
  		foundFile.writeTo("E:/LiftDrill/src/main/webapp/bigdata/22.scala")
  }*/

	
}