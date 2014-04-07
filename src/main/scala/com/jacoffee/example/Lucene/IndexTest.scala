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


// 学习如何建立 index
class  IndexTest  {


		
		// prepare materials for indexing
		protected val ids = Array("1", "2")
		protected val unindexed = Array( "Netherlands", "Italy")
		protected val unstored = Array("Amsterdam has lots of bridges", "Venice has lots of canals")
		protected val text = Array("Amsterdam", "Venice")
		
		// directory to write the index to 
		//TODO
		// 使用memory 进行缓存的话  每一次运行都要进行 index  否则会被清理的  
		val indexedFilePosition = FSDirectory.open(new File("D:/luceneindex"))
		// build  document and establish index
		
		// encapsulate the detail of IndexWriter
		def getIndexWriter =  {
				// config && analyzer
				val config = new IndexWriterConfig(Version.LUCENE_34, new WhitespaceAnalyzer)
				// write to where  and how to write
				new IndexWriter(indexedFilePosition, config)
				
				/* About  IndexWriter的锁机制 */
				// 调用上面的方法构造一个新的IndexWriter 并且每次都会上锁  通过	iWriter.close 释放锁之后
				// 才能再进行新建
				//
				/* About  IndexWriter针对文件的写入方式*/
	// IndexWriterwill detect that there’s no prior index in this Directoryand create a new
	// one. If there were an existing index, IndexWriterwould simply add to it.
	// IndexWriter会看相应的目录是否已经有了 索引相关的文件 如果有了 机会直接往后面添加 
	//  这也就是为什么 在前一个程序中不断运行  会不断添加新的 索引文件
		}
		
		 def  addDocuments = {
				val iWriter = getIndexWriter
				for(i <- 0  until ids.length ) {
					val doc = new Document()  //一定要注意这个的位置 如果放上面了
					// 就会造成 id ： 1、2 而非 id ：1   id：2  这是因为lucene允许field同名
					doc.add(new Field("id", ids(i), Store.YES, Index.NOT_ANALYZED))
					doc.add(new Field("country", unindexed(i), Store.YES, Index.NO))
					doc.add(new Field("contents", unstored(i), Store.NO, Index.ANALYZED))
					doc.add(new Field("city", text(i), Store.YES, Index.ANALYZED))
					iWriter.addDocument(doc)
				}
				// 写入两个document
				println( iWriter.numDocs() )
				// 打印文档的ID
				iWriter.close
		}
		
		
		// query the index  《必须要给子类调用》
		 def getHitCount(fieldName: String,  searchString: String) = {
				// search from where  in the memory instead of FS
				val iSearch = new IndexSearcher(indexedFilePosition)
				// term Query
				val termQuery = new TermQuery(new Term(fieldName, searchString))
				val topDocs = iSearch.search(termQuery, 10)
				// 看看文档ID到底是什么样的
				println(topDocs.scoreDocs.map(_.doc))
				topDocs.totalHits
		}
		
		
		// remove index 
		// conditon For instance, a newspaper publisher may want to keep only the 
		//  last week’s worth of news in its searchable indexes.		
		
		// 删除文档  
		// if you intend to delete a single document by Term, you must ensure 
		//  you’ve indexed a Field on every document
		// 其实这个也很好 理解因为 如果你查询出来就无法删除  在lucene中如果你不索引就无法删除 所以必须索引
		
		//l另外在选择 term 的时候也需要注意 确保和 db一样要唯一
		// 并且不能被 分词 
		// This field should beindexed as an unanalyzed field  to ensure the analyzer doesn’t break it up into separate tokens.
		//  new Term("ID", documentID)
		def removeDocuments(t: Term) = {
				val writer = getIndexWriter   
				println("删除操作之前的 文档数目 " + writer.numDocs )
				writer.deleteDocuments(t)
				// 注意  numDocs (the number of undeleted documents in an index.)和 
				// maxDocs（including both deteled and undeleted）
				
				// very imortant step 
					//  because after a delete and optimize, Lucene truly removes the deleted document.
				writer.optimize
				writer.commit
				println("标记为已经删除的 after " +  writer.hasDeletions ) //  true  -> false optimize(内存被清理了 所以记不得有删除的东西了)
				println("总共的 maxDocs " + writer.maxDoc)  // 2
				println("只统计未被删除的 " + writer.numDocs)  // 1
				writer.close
		}
		
		
		// 更新逻辑 先删除查询出来的文档  然后 以新文档替换
		 def updateDocument  =  {
				//assertEquals(1, getHitCount("city", "Amsterdam"))
				val writer =  getIndexWriter
				val doc = new Document
				doc.add(new Field("id", "3", Store.YES, Index.NOT_ANALYZED))
				doc.add(new Field("country", "China", Store.YES, Index.NO))  // 此时便不能使用TermVector
				doc.add(new Field("contents", "Den Haag has a lot of museums", Store.NO, Index.ANALYZED))
				doc.add(new Field("city", "Den Haag", Store.YES, Index.ANALYZED))
				writer.updateDocument(new Term("id", "1"), doc)
				writer.close
				//因为已经被更新了所以应该找不到了
				//assertEquals(1, getHitCount("city", "Amsterdam"))
		}
		
}


object  LuceneTest extends IndexTest  with App{
		//val it = new IndexTest
		//it.addDocuments
		//it.removeDocuments(new Term("id","1"))
		// 查
		//println("adasd" + it.getHitCount("city", "Amsterdam"))  // docid  --  [I@1ffc686
		// it.removeDocuments(new Term("id","1"))
		// it.removeDocuments(writer, new Term("country", "Netherlands"), writer.optimize)
		println ( Margin.values.map(v => v.id + " : " + v).mkString )
}


		//  Scala 集合优雅的遍历, 筛选, 映射实际上是建立在底层 "Java式的for循环的基础之上"的 
		//  所以知道  底层的实现 比 随心所欲的使用更加重要 

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
			// 将首尾提出来考虑 所以构成了如下形式 start  ele sep ele sep ele sep  end
			//  剔除第一个ele就是 sep ele的节奏 所以需要在 是否第一个判断的时候 进行判断
			var firstAppend_? = true
			b append start
			for(ele  <- self) {
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

