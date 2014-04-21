package com.jacoffee.example.Lucene

import java.io.{StringReader, File}
import java.util.Calendar
import org.apache.lucene.store.{ Directory, FSDirectory }
import org.apache.lucene.index.{ Term, IndexWriter, IndexWriterConfig }
import org.apache.lucene.analysis.{ WhitespaceAnalyzer, SimpleAnalyzer }
import org.apache.lucene.util.Version
import org.apache.lucene.document.{ Document, Field, NumericField }
import org.apache.lucene.document.Field.{ Store, Index }
import org.apache.lucene.search.{ IndexSearcher, TermQuery, Filter, NumericRangeQuery, PrefixQuery, BooleanQuery, BooleanClause, MatchAllDocsQuery }
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.analysis.standard.StandardAnalyzer

// use Enumeration in Scala
/** Creates a fresh value, part of this enumeration. */
 // protected final def Value: Value = Value(nextId)  被子类继承的
object Margin extends Enumeration {
	type Margin = Value
	val TOP, BOTTOM, LEFT, RIGHT = Value  // 默认的int 从0开始排
	val top = Value(10, "TOP")
	val bottom = Value(20, " BOTTOM")
	val left = Value(30, "Left")
	val right = Value(40, "RIGHT")
}


object myMkString  {
	val self = List("Hello", "World", "Welcome")
	def addString(start: String="", sep: String, end: String="") = {
		val b = new StringBuilder
		//  将首尾提出来考虑 所以构成了如下形式 start  ele sep ele sep ele sep  end
		//  剔除第一个ele就是 sep ele的节奏 所以需要在 是否第一个判断的时候 进行判断
		var firstAppend_? = true
		b append start
		for (ele  <- self) {
			if(firstAppend_?) {
				 b append ele
				 firstAppend_? = false
			} else {
				b append sep
				b append  ele
			}
		}
		b append end
		b.mkString
	}
}

// 学习如何建立 index
class  Indexable  {
	// prepare materials for indexing
	protected val ids = Array("1", "2", "3", "4", "5")
	protected val unindexedCountry = Array( "Netherlands", "Italy", "China", "US", "Chile")
	protected val unindexedCity = Array("Amsterdam", "Venice", "Shanghai", "Los Angeles", "Santiago")
	protected val unstoredDesp = Array(
		"阿姆斯特丹是个优美的地方",
		"威尼斯是个水上城市",
		"上海是中国的文化和政治中心",
		"洛杉矶有很多的NBA球星",
		"智利的足球世界闻名"
	)
	protected val population = Array(100, 231, 245, 267, 120)
	protected val foundTime = Array(1913, 1867,1949, 1776, 1345)

	// directory to write the index to
	// XXX  Take Care When Storing Index in Memory, It will be cleared so you have to build Index iteratively
	val indexedFilePosition = FSDirectory.open(new File("D:/Lucene/Country"))
	val version = Version.LUCENE_34

	// build  document and establish index
	def getIndexWriter: IndexWriter =  {
		/* About  IndexWriter针对文件的写入方式*/
		// IndexWriterwill detect that there’s no prior index in this Directory and create a new one. If there were an existing index, IndexWriter would simply add to it.
		// IndexWriter会看相应的目录是否已经有了 索引相关的文件 如果有了, 就会直接往后面添加;这也就是为什么 在前一个程序中不断运行会不断添加新的索引文件

		// config && analyzer
		val config = new IndexWriterConfig(version, new WhitespaceAnalyzer(version))
		// write to where  and how to write
		/* About  IndexWriter的锁机制 */
		new IndexWriter(indexedFilePosition, config)
		// 调用上面的方法构造一个新的IndexWriter 并且每次都会上锁  通过iWriter.close 释放锁之后才能再进行新建
	}

	 def  addDocuments {
		val iWriter = getIndexWriter
		def getCalendarTime(year: Int) = {
			val cal = Calendar.getInstance
			cal.set(Calendar.YEAR, year)
			cal.getTimeInMillis
		}

		for(i <- 0  until ids.length ) {
			val doc = new Document()
			val cityName = unindexedCity(i)
			doc.add(new Field("id", ids(i), Store.YES, Index.NOT_ANALYZED))
			doc.add(new Field("country", unindexedCountry(i), Store.YES, Index.NO))
			doc.add(new Field("city", cityName, Store.YES, Index.ANALYZED))
			doc.add(new Field("contents", unstoredDesp(i), Store.NO, Index.ANALYZED))
			// Index Numeric Value in Lucene
				// -> pure number
			val pop = new NumericField("population").setIntValue(population(i))
			val found =new NumericField("foundTime").setLongValue(getCalendarTime(foundTime(i)))
				// ->Date time String
/*			val foundTime = new NumericField("foundTime").setLongValue{
				setCalendar(-((i+1)*100)).getTimeInMillis
			}
*/			doc.add(pop)
			doc.add(found)

			// doc.add(new NumericField
			if (cityName == "China") { doc.setBoost(1.5f) } else { doc.setBoost(1.0f) }
			iWriter.addDocument(doc)
		}
		println(" 新增加的文档数: " + iWriter.numDocs() )
		iWriter.close // commit changes to Directory
	}

	// query the index 《必须要给子类调用》
	 def getHitCount(fieldName: String,  searchString: String) = {
		 val iSearch = new IndexSearcher(indexedFilePosition)
		// term Query -- equals to exact match In SQL
		val termQuery = new TermQuery(new Term(fieldName, searchString))
		val topDocs = iSearch.search(termQuery, 5)
		// 看看文档ID到底是什么样的
		 println(" 所有命中的文档的Id " + topDocs.scoreDocs.toList.map(_.doc)) // docId starts from one
		 println(" 所有命中的文档的Score " + topDocs.scoreDocs.toList.map(_.score))


		// Explaination  -- get all the whistles and bells of scoring
		val scoreTexts = topDocs.scoreDocs.toList.map { macthed =>
			iSearch.explain(termQuery, macthed.doc)
		}
		println( scoreTexts.map(_.toHtml) )

		topDocs.totalHits
	}

	def testQueryParser(fieldName: String,  searchString: String) = {
		val iSearch = new IndexSearcher(indexedFilePosition)
		// query parser translate user-enter query expression into query object for IndexSearcher to execute
		// 确保和索引文档时所用的 分词器一致
		val parser = new QueryParser(version, fieldName, new WhitespaceAnalyzer(version))
		// "Amsterdam Or Shanghai"
		val parsedQuery = parser.parse(searchString)
		val topDocs = iSearch.search(parsedQuery, 5)
		val docId = topDocs.scoreDocs.toList.map(_.doc)
		println(" 命中的ID数 "  + docId)
	}

	def testNumericRangeQuery(fieldName: String) = {
		val iSearch = new IndexSearcher(indexedFilePosition)
		val query = NumericRangeQuery.newIntRange(fieldName, 100, 250, true, true)
		val topDocs = iSearch.search(query, 10)
		println(" 人口在 100 -250的城市有几个 ")
		println(topDocs.totalHits)
	}

	def testBooleanQuery = {
		val iSearch = new IndexSearcher(indexedFilePosition)
		val booleanQuery = new BooleanQuery
		val rangeQuery = NumericRangeQuery.newIntRange("population", 100, 250, true, true)
		val prefixQuery = new PrefixQuery(new Term("city", "S"))
		// 查询城市名中以S开头并且人口在100-250的文档
		booleanQuery.add(rangeQuery, BooleanClause.Occur.MUST)
		booleanQuery.add(prefixQuery, BooleanClause.Occur.MUST)
		val topDocs = iSearch.search(booleanQuery, 5)
		println("查询城市名中以S开头并且人口在100-250的文档数目")
		println(" 总数 " + topDocs.totalHits)
		println(" 命中文档编号" + topDocs.scoreDocs.toList.map(_.doc))
		println(" 命中文档城市名" +
			topDocs.scoreDocs.toList.map(_.doc).map { docId =>
				iSearch.doc(docId).get("city")
			}
		)
		println("all the document " +iSearch.search(new MatchAllDocsQuery, 5).totalHits )
	}

/*	// with default SimpleSpanFragmenter
	def testHighlighting = {
		val textToDivide = "The quick brown fox jumps over the lazy dog"
		val query = new TermQuery(new Term("field", "fox"));

		// token Stream definitely generated by Analyzer
		val tokenStream = new SimpleAnalyzer(version).tokenStream("field", new StringReader(textToDivide))

		// score only those terms that participated in generating the hit on the document
		val scorer = new QueryScorer(query, "field")

		val fragmenter = new SimpleSpanFragmenter(scorer)

		//  To successfully highlight terms, the terms in the Queryneed to match Tokens emitted from the TokenStream
		//         为了成功的高亮 查询的Term必须和 Analyze分词的Term匹配上
		val highlighter = new Highlighter(scorer)

		// user what kind of fragmenter to fragment the hit text <span></span> or <b></b>
		highlighter.setTextFragmenter(fragmenter)

		// display the final result accroding the tokenStream query
		val result = highlighter.getBestFragment(tokenStream, textToDivide)

		result
	}

	// wth customized hightling fragmenter that is wrap the hit term in TokenStream with our defined style
	// such as wrap it like <span class="highlight">fox</span>
	def testHightingIt = {
		val textToDivide = "In this section we'll show you how to make the simplest " +
		"programmatic query, searching for a single term, and then " +
		"we'll see how to use QueryParser to accept textual queries. " +
		"In the sections that follow, we’ll take this simple example " +
		"further by detailing all the query types built into Lucene. " +
		"We begin with the simplest search of all: searching for all " +
		"documents that contain a single term."
		val tokenStream = new StandardAnalyzer(Version.LUCENE_30).tokenStream("f", new StringReader(textToDivide));
		val searchString = "term"
		val parser = new QueryParser(version, "f", new WhitespaceAnalyzer(version))
		// "Amsterdam Or Shanghai"
		val parsedQuery = parser.parse(searchString)

		val scorer = new QueryScorer(parsedQuery, "f")
		val formatter = new SimpleHTMLFormatter("<span class='highlight'>", "</span>")
		val highlighter = new Highlighter(formatter, scorer)
		highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer))
		// pay attention to getBest   means there are possibly only one hit (cause most relevant)
		//val result2 = highlighter.getBestFragment(tokenStream, textToDivide)
		// result2
		// <span class='highlight'>term</span>, and then we'll see how to use QueryParser to accept textual queries. In the sections
		val allresult = highlighter.getBestFragments(tokenStream, textToDivide, 3, "...")
		allresult
	}*/
	// remove index
	// conditon For instance, a newspaper publisher may want to keep only the last week’s worth of news in its searchable indexes.

	// 删除文档
	// if you intend to delete a single document by Term, you must ensure
	//  you’ve indexed a Field on every document
	// 其实这个也很好 理解因为 如果你查询出来就无法删除  在lucene中如果你不索引就无法删除 所以必须索引

	//另外在选择 term 的时候也需要注意 确保和db一样要唯一并且不能被分词
	// This field should beindexed as an unanalyzed field  to ensure the analyzer doesn’t break it up into separate tokens.
	// new Term("ID", documentID)
	def removeDocuments(t: Term) = {
		val writer = getIndexWriter
		println("删除操作之前的 文档数目 " + writer.numDocs )
		writer.deleteDocuments(t)
		// 注意  numDocs (the number of undeleted documents in an index.)和
		// maxDocs (including both deteled and undeleted)

		// very imortant step  ||  because after a delete and optimize, Lucene truly removes the deleted document.
		writer.optimize
		writer.commit
		println("标记为已经删除的 after " +  writer.hasDeletions ) //  true  -> false optimize(内存被清理了 所以记不得有删除的东西了)
		println("总共的 maxDocs " + writer.maxDoc)  // 2
		println("只统计未被删除的 " + writer.numDocs)  // 1
		writer.close
	}

	// 更新逻辑 先删除查询出来的文档  然后 以新文档替换
	 def updateDocument = {
		//assertEquals(1, getHitCount("city", "Amsterdam"))
		val writer =  getIndexWriter
		val doc = new Document
		doc.add(new Field("id", "1", Store.YES, Index.NOT_ANALYZED))

		// Don't index the field so it will not be searched
		doc.add(new Field("country", "Korean", Store.YES, Index.NO))

		// index the tokens analyzed  commonly for big-binary text
		doc.add(new Field("contents", "Korean likes to Claim to everythings belong to them", Store.NO, Index.ANALYZED))
		doc.add(new Field("city", "Seoul", Store.YES, Index.ANALYZED))

		/*
			first param -->  Term  term the term to identify the document(s) to be deleted
			Term -->
		 */
		writer.updateDocument(new Term("id", "1"), doc)
		writer.close
		// 因为已经被更新了所以应该找不到了
		// assertEquals(1, getHitCount("city", "Amsterdam"))
		doc
	}

	def updateDocument(doc: Document) = {
	}
}


object LuceneTest extends Indexable with App {
	// Index Document -- addDocuments 反复添加会不断的出现
	/*
		Query Document
		println(" Query The City " + getHitCount("city", "Amsterdam"))
	*/
	/*
		println(" Has Deletions 检查磁盘是否有被标记为删除的Document")
		println(getIndexWriter.hasDeletions)
	*/
	// it.removeDocuments(new Term("id","1"))
	//println(" IndexWriter 对应的Docs " +getIndexWriter.numDocs)
	// it.removeDocuments(writer, new Term("country", "Netherlands"), writer.optimize)
	/*
		Update Lucene Index
		updateDocument 一句话 先删后更新 || 更新的
	*/
	//AMSTERDAM
	// getHitCount("city", "Amsterdam")  // case incentive
	// testQueryParser("city", "Amsterdam Or Shanghai")
	// testNumericRangeQuery("population")
	// testBooleanQuery
	addDocuments
}
