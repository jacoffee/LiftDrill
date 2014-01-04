package net.liftweb.example.record


import com.mongodb.Mongo
import com.mongodb.ServerAddress
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.DefaultMongoIdentifier


// this Object is for the connection to mongodb 
object Config {
		
		  def initConnection = {
						val server = new ServerAddress("localhost",27017)
						// for Mongo Class
						//    A database connection with internal connection pooling,  自带连接池的 数据库连接 
						//    For most applications,  you should have one Mongo instance for the entire JVM.				
						MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(server), "resume")
		  }			
}