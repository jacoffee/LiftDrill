package com.jacoffee.example.Lucene

import org.apache.lucene.store.Directory
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.analysis.WhitespaceAnalyzer
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.Field.Index
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TermQuery
import org.apache.lucene.index.Term
import org.apache.lucene.store.LockObtainFailedException
import org.apache.lucene.store.FSDirectory
import java.io.File
import scala.collection.SeqLike

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
	protected val ids = Array("1", "2", "3", "4")
	protected val unindexedCountry = Array( "Netherlands", "Italy", "China", "US")
	protected val unindexedCity = Array("Amsterdam", "Venice", "Shanghai", "Los Angeles")
	protected val unstoredDesp = Array("Amsterdam has lots of bridges", "Venice has lots of canals", "ShangHai is the Cultural&Education center of China",  "LA got a bunch of talented NBA stars")

	// directory to write the index to
	// XXX  Take Care When Storing Index in Memory, It will be cleared so you have to build Index iteratively
	val indexedFilePosition = FSDirectory.open(new File("D:/Lucene/Country"))

	// build  document and establish index
	def getIndexWriter: IndexWriter =  {
		/* About  IndexWriter针对文件的写入方式*/
		// IndexWriterwill detect that there’s no prior index in this Directory and create a new one. If there were an existing index, IndexWriter would simply add to it.
		// IndexWriter会看相应的目录是否已经有了 索引相关的文件 如果有了, 就会直接往后面添加;这也就是为什么 在前一个程序中不断运行会不断添加新的索引文件

		// config && analyzer
		val version = Version.LUCENE_34
		val config = new IndexWriterConfig(version, new WhitespaceAnalyzer(version))
		// write to where  and how to write
		/* About  IndexWriter的锁机制 */
		new IndexWriter(indexedFilePosition, config)
		// 调用上面的方法构造一个新的IndexWriter 并且每次都会上锁  通过iWriter.close 释放锁之后才能再进行新建
	}

	 def  addDocuments {
		val iWriter = getIndexWriter
		for(i <- 0  until ids.length ) {
			val doc = new Document()
			doc.add(new Field("id", ids(i), Store.YES, Index.NOT_ANALYZED))
			doc.add(new Field("country", unindexedCountry(i), Store.YES, Index.NO))
			doc.add(new Field("city", unindexedCity(i), Store.YES, Index.ANALYZED))
			doc.add(new Field("contents", unstoredDesp(i), Store.NO, Index.ANALYZED))
			iWriter.addDocument(doc)
		}
		// 写入两个document
		// 打印文档的ID
		println(" 新增加的文档数: " + iWriter.numDocs() )
		iWriter.close // commit changes to Directory
	}

	// query the index 《必须要给子类调用》
	 def getHitCount(fieldName: String,  searchString: String) = {
		// search from where  in the memory instead of FS
		val iSearch = new IndexSearcher(indexedFilePosition)
		// term Query -- equals to exact match In SQL
		val termQuery = new TermQuery(new Term(fieldName, searchString))
		val topDocs = iSearch.search(termQuery, 5)
		// 看看文档ID到底是什么样的
		 println(" 所有命中的文档的Id " + topDocs.scoreDocs.toList.map(_.doc)) // docId starts from one
		 println(" 所有命中的文档的Score " + topDocs.scoreDocs.toList.map(_.score))
		topDocs.totalHits
	}

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
	// Index Document -- addDocuments
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
}
