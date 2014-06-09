package com.jacoffee.example.model

import java.io.{Reader, File}
import scala.concurrent.{ Await, ExecutionContext, Future, future }
import org.apache.lucene.document.{ Document, Field, Fieldable }
import org.apache.lucene.document.Field.{ TermVector, Index, Store }
import org.apache.lucene.search.{ MatchAllDocsQuery, Sort, SortField, Query, IndexSearcher }
import org.apache.lucene.index.{Term, IndexWriter, IndexWriterConfig, IndexReader}
import org.apache.lucene.store.FSDirectory
import com.jacoffee.example.util.Config.Lucene.{ version, getIndexedFilePosition, getStopWordsSet, smartChineseAnalyzer }
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.queryParser.MultiFieldQueryParser
import org.apache.lucene.search.highlight.{SimpleSpanFragmenter, SimpleHTMLFormatter, Highlighter, QueryScorer}
import org.bson.types.ObjectId
import com.jacoffee.example.util.{ TempCache, Helpers }


/**
 * Created by qbt-allen on 14-6-6.
 */
trait IndexableModel[ModelType <: IndexableModel[ModelType]] extends MongoModel[ModelType] { self: ModelType =>
	val idIndexFieldName = idFieldName
	def idIndexFieldValue = idValue
	def indexFields: List[Fieldable]
	def index = {
		val document = new Document
		(
			new Field(idIndexFieldName, idIndexFieldValue.toString, Store.YES, Index.ANALYZED, TermVector.NO) :: indexFields
		).foldLeft(document)(
			(doc, field) => {
				doc.add(field)
				doc
			}
		)
	}
	protected def getIndexFields(fieldName: String, fieldValue: String,
		store: Store, index: Index, termVector: TermVector,boostOption: Option[Float]) = {
		val field = new Field(fieldName, fieldValue, store, index, termVector)
		boostOption.foreach(field.setBoost)
		field
	}
	// mainly for query, typical example is intention_id in the SeekJob you just wanna query
	def getNotIndexFields(fieldName: String, fieldValue: String) = {
		new Field(fieldName, fieldValue, Store.NO, Index.NOT_ANALYZED_NO_NORMS, TermVector.NO)
	}
	override def afterSave {
		super.afterSave
	}
}

trait IndexableModelMeta[ModelType <: IndexableModel[ModelType]] extends IndexableModel[ModelType]
	with MongoModelMeta[ModelType]
	with LuceneUtil {
	self:ModelType =>

	protected val skip = 0
	protected val limit = 10
	def indexModel(model: ModelType) = model.index

	// 根据Lucene Document 查询出所有的 objectids   --- 如果在数据库中找不到对应的则删除 否则就更新相应的索引
	def indexOne(modelId: ObjectId) { indexAll(List(modelId)) }
	def indexAll(modelIds: List[ObjectId]) {
		import ExecutionContext.Implicits.global
		future {
			val indexWriter = getIndexWriter
			createIndex(indexWriter) {
				val modelsById = findAll(modelIds).groupBy(_.idIndexFieldValue)
				modelIds.grouped(10).foreach { groupedDbIds =>
					groupedDbIds.foreach { groupedDbId =>
						modelsById.get(groupedDbId).flatMap(_.headOption) match {
							case Some(model) => {
								indexWriter.updateDocument(new Term(idFieldName, model.idValue.toString), indexModel(model))
							}
							case _ => indexWriter.deleteDocuments(new Term(idFieldName, groupedDbId.toString))
						}
					}
				}
			}
		}
	}

	// Update Lucene Index

	/* Search Lucene Index */
	/* Build Query Info
	* Pay attention to the Constructor
	* */
	case class SortInfo(fieldName: String, sortType: Int = SortField.LONG, reverse: Boolean= false)
	class QueryInfo(query: Query, sortOption: Option[Sort]) {
		// Also, each constructor must call one of the previously defined constructors.
		// one arg
		def this(query: Query, sort: Sort) = this(query, Some(sort))
		def this(query: Query) = this(query, None)
		def this(query: Query, sortInfo: SortInfo) =
			this(
				query,
				new Sort(new SortField(sortInfo.fieldName, sortInfo.sortType, sortInfo.reverse))
			)
	}

	def getAllDocuments = luceneSearch("", new MatchAllDocsQuery, Array.empty)
	/**
	 * Mainly for lucene searcher process
	 * @param search
	 * @param query
	 * @param fieldNameArray
	 * @param sortInfoOption
	 * @return hit Document and the Total Number
	 */
	def luceneSearch(search: String, query: Query, fieldNameArray: Array[String], sortInfoOption: Option[SortInfo] = None) = {
		val parsedQuery = {
			if (search.isEmpty && fieldNameArray.isEmpty) {
				new MatchAllDocsQuery
			} else {
				new MultiFieldQueryParser(version, fieldNameArray, smartChineseAnalyzer).parse(search)
			}
		}
		val indexSearcher = cachedIndexSearcher.get
		val topDocs = sortInfoOption match {
			case Some(sortInfo) => {
				indexSearcher.search(
					parsedQuery,
					limit,
					new Sort(new SortField(sortInfo.fieldName, sortInfo.sortType, sortInfo.reverse))
				)
			}
			case _ => indexSearcher.search(parsedQuery, limit)
		}
		(
			topDocs.scoreDocs.map(scoreDoc =>
				indexSearcher.doc(scoreDoc.doc)
			),
			topDocs.totalHits
		)
	}

	def highlightText(query: Query, fieldName: String, textToDivide: String) = {
		//val parser = new MultiFieldQueryParser(version, fieldNameArray, smartChineseAnalyzer)
		// val parsedQuery = parser.parse(search)
		val scorer = new QueryScorer(query, null)
		val highlighter = new Highlighter(
			new SimpleHTMLFormatter("""<span class="highlight">""", """</span>"""),
			scorer
		)
		// 1000 stands for the size of byte that will be each hit
		def toOptional(text: String) = if (Helpers.isBlank(text)) None else Some(text)
		def geBestFragment(textToDivide: String) = toOptional(highlighter.getBestFragment(smartChineseAnalyzer, fieldName, textToDivide))
		highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer,1000))
		// @return highlighted text fragment or null if no terms found so you have another consideration
		geBestFragment(textToDivide)
	}

	def getIndexModels(queryText: String, query: Query, fieldNameArray: Array[String], sortInfoOption: Option[SortInfo] = None) = {
		val (docArray, totalHits) = luceneSearch(queryText, query, fieldNameArray, sortInfoOption)
		val objectIds =docArray.flatMap(doc => toObjectIdOption(doc.get(idIndexFieldName))).toList
		// XXX Pay attention: this is will be ordered by ids so the lucene sort takes effect but overriden by DataBase order
		val modelsById = findIn(objectIds).groupBy(_.idValue)
		(objectIds: List[ObjectId]).flatMap(id => modelsById.get(id).flatMap(_.headOption))
	}

}

trait LuceneUtil {

	val cachedPeriod = 1000L * 60
	//protected val collectionName: String = ""
	protected val indexName = "temp"
	def directory = FSDirectory.open(new File(getIndexedFilePosition(indexName)))

	object cachedIndexSearcher extends TempCache(cachedPeriod)(new IndexSearcher(IndexReader.open(directory, true)))

	def getIndexWriter = {
		// if article already exists in the Lucene Update orElse index
		// you should consider refresh period
		val config = new IndexWriterConfig(version, smartChineseAnalyzer)
		new IndexWriter(directory, config)
	}

	def createIndex(writer: IndexWriter)(indexing:  => Any) {
		try {
			indexing
			writer.close
		} finally {
			if (IndexWriter.isLocked(directory)) {
				IndexWriter.unlock(directory)
			}
		}
	}

//	def createIndex(doc: Document) = {
//		val indexWriter = getIndexWriter
//		indexWriter.addDocument(doc)
//		try {
//			indexWriter.close
//		} finally {
//			if (IndexWriter.isLocked(directory)) {
//				IndexWriter.unlock(directory)
//			}
//		}
//	}
	def AnalyzerUtils(analyzer: Analyzer, reader: Reader) = {
		val stream = analyzer.reusableTokenStream("", reader)
		val term = stream.addAttribute(classOf[CharTermAttribute])
		Stream.continually((stream.incrementToken, term.toString)).takeWhile(_._1).map(t =>s"[${t._2}]")
	}
}
