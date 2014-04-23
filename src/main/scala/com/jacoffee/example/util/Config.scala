package com.jacoffee.example.util

import org.apache.lucene.util.Version
import net.liftweb.util.Props
import net.liftweb.mongodb.MongoIdentifier

/**
 * Created by qbt-allen on 14-4-23.
 * for Project  Config
 */
object Config {


	object Path {
		val path_shared = Props.get("path_shared").openOrThrowException {
			"lucene_path must define in default.props"
		}
	}

	object Lucene {
		val version = Version.LUCENE_34
		val path = "lucene"
	}

	object Mongo {
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
}



