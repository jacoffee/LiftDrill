package com.jacoffee.example.util

import org.apache.lucene.util.Version
import net.liftweb.util.Props
import net.liftweb.mongodb.MongoIdentifier
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer
import scala.io.{Codec, Source}
import org.apache.lucene.analysis.WordlistLoader

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

	class Lucene {}
	object Lucene {
		val version = Version.LUCENE_34
		val path = "lucene"
		def getIndexedFilePosition(indexName: String) = (Path.path_shared :: path :: indexName :: Nil).mkString("\\")
		def getStopWordsSet = {
			//Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("lucene/stopwords.txt"))(Codec.UTF8).getLines.toSet
			val stopWordSrc = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("lucene/stopwords.txt"))(Codec.UTF8)
			// println("Orginal Ones" +stopWordSrc.getLines.toList)
			try {
				WordlistLoader.getWordSet(classOf[Lucene], "/lucene/stopwords.txt", "//")
			} finally {
				stopWordSrc.close
			}
		}
		val smartChineseAnalyzer = new SmartChineseAnalyzer(version, getStopWordsSet)
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



