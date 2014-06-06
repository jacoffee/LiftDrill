package com.jacoffee.example.model

import java.io.{Reader, File}
import org.apache.lucene.document.{ Document, Field, Fieldable }
import org.apache.lucene.document.Field.{ TermVector, Index, Store }
import org.apache.lucene.search.{ Sort, SortField, Query, IndexSearcher }
import org.apache.lucene.index.IndexReader
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
			new Field(idIndexFieldName, idIndexFieldValue.toString, Store.NO, Index.ANALYZED, TermVector.NO) :: indexFields
		).foldLeft(document)(
			(doc, field) => {
				doc.add(field)
				doc
			}
		)
	}
	protected def getIndexFields(fieldName: String, fieldValue: String,
		store: Store, index: Index, termVector: TermVector,boostOption: Option[Float]) = {
		val field = new Field(idFieldName, fieldValue, store, index, termVector)
		boostOption.foreach(field.setBoost)
		field
	}
	def getNoIndexFields(fieldName: String, fieldValue: String) = {
		new Field(idFieldName, fieldValue, Store.NO, Index.NOT_ANALYZED_NO_NORMS, TermVector.NO)
	}
}

trait IndexableModelMeta[ModelType <: IndexableModel[ModelType]] extends IndexableModel[ModelType]
	with MongoModelMeta[ModelType]
	with LuceneUtil {
	self:ModelType =>

	protected val skip = 0
	protected val limit = 10
	def indexModel(model: ModelType) = model.index

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

	/**
	 * Mainly for lucene searcher process
	 * @param search
	 * @param query
	 * @param fieldNameArray
	 * @param sortInfoOption
	 * @return hit Document and the Total Number
	 */
	def search(search: String, query: Query, fieldNameArray: Array[String], sortInfoOption: Option[SortInfo] = None) = {
		val parsedQuery = new MultiFieldQueryParser(version, fieldNameArray, smartChineseAnalyzer).parse(search)
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
//		val objectIds =topDocs.scoreDocs.toList.map { hitDoc =>
//			val actualDoc = indexSearcher.doc(hitDoc.doc)
//			val termVectorAndFreq = indexSearcher.getIndexReader.getTermFreqVector(hitDoc.doc, "content")
//			actualDoc.get(id.name)
//		}
//		// XXX Pay attention: this is will be ordered by ids so the lucene sort takes effect but overriden by DataBase order
//		val modelsById = findIn(objectIds).groupBy(_.idValue)
//		(objectIds: List[ObjectId]).flatMap(id => modelsById.get(id).flatMap(_.headOption))
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

	def getIndexModels(search: String, query: Query, fieldNameArray: Array[String], sortInfoOption: Option[SortInfo] = None) = {



	}
}

trait LuceneUtil {

	val cachedPeriod = 1000L * 60
	protected val collectionName: String
	val indexedFilePosition = getIndexedFilePosition(collectionName)
	val directory = FSDirectory.open(new File(indexedFilePosition))

	object cachedIndexSearcher extends TempCache(cachedPeriod)(new IndexSearcher(IndexReader.open(directory, true)))

	def AnalyzerUtils(analyzer: Analyzer, reader: Reader) = {
		val stream = analyzer.reusableTokenStream("", reader)
		val term = stream.addAttribute(classOf[CharTermAttribute])
		Stream.continually((stream.incrementToken, term.toString)).takeWhile(_._1).map(t =>s"[${t._2}]")
	}
}
