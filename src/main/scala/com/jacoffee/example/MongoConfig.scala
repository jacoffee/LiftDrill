package net.liftweb.example

import net.liftweb.util.Props
import java.io.FileInputStream
import net.liftweb.common.Full
import net.liftweb.mongodb.MongoIdentifier



// settings for Mongo
object MongoConfig {
	// D:\LiftDrill\src\main\resources\props\mongo.props
	val filename = System.getProperty("user.dir") + System.getProperty("file.separator") + "src\\main\\resources\\props\\mongo.props"
	//   @volatile var whereToLook: () => List[(String, () => Box[InputStream])] = () => Nil
	Props.whereToLook = () => 
		(
			filename, 
			() => Full(new FileInputStream(filename)) 
	):: Nil
	println(filename)
	val host = Props.get("mongo_host").openOrThrowException {
		"the mongo host MUST be defined in props"
	}
	val port = Props.getInt("mongo_port").openOrThrowException {
		"valid mongo port MUST be defined in props"
	}
	val db = Props.get("mongo_db").openOrThrowException {
		"the mongo db MUST be defined in props"
	}
	val connectionsPerHost = Props.getInt("mongo_connectionsPerHost").openOr(10)
	val threadsAllowedToBlockForConnectionMultiplier =
		Props.getInt("mongo_threadsAllowedToBlockForConnectionMultiplier").openOr(5)
    
    object DefaultMongoIdentifier extends MongoIdentifier {
    	val jndiName = "default"
    }
}