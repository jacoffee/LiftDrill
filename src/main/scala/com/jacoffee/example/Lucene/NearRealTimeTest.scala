package com.jacoffee.example.Lucene

import org.apache.lucene.store.{ Directory, FSDirectory, RAMDirectory }
import org.apache.lucene.index.{ IndexReader, IndexWriter, IndexWriterConfig, Term }
import org.apache.lucene.analysis.SimpleAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.document.{ Document, Field, NumericField }
import org.apache.lucene.document.Field.{ Store, Index }
import org.apache.lucene.search.{ IndexSearcher, TermQuery }

object NearRealTimeTest extends App {

	def testNearRealTime = {
		// build index
		val version = Version.LUCENE_34
		val dir = new RAMDirectory
		val config = new IndexWriterConfig(version, new SimpleAnalyzer(version))
		val writer = new IndexWriter(dir, config)
		val range: Range = (1 to 10)
		range.map { num =>
			val doc = new Document
			doc.add(new Field("id", num.toString, Store.YES, Index.NOT_ANALYZED))
			doc.add(new Field("text", "aaa", Store.NO, Index.ANALYZED_NO_NORMS))
			writer.addDocument(doc)
		}

		// build IndexReader
		//  boolean applyAllDeletes的作用 首先Lucene的Delete并不是真正的删除只是将状态标识为 已删除
		//  然后这个Boolean的值将会影响到是否 被删除的文档是否会反应到 返回的Reader 通俗的说就是 是否在返回的Reader中有这个值
		//  If true, all buffered deletes will  be applied (made visible) in the returned reader.
		// true 就是9个 false 就是10个
		val reader = IndexReader.open(writer, true)
		var indexSearcher = new IndexSearcher(reader)
		val query = new TermQuery(new Term("text", "aaa"))
		val topDocs = indexSearcher.search(query, 1)

		println("所有命中文档是10吗？")
		println(topDocs.totalHits == range.length)

		// delete operation
		writer.deleteDocuments(new Term("id", "7"))

		// add one document
/*		val doc = new Document();
		doc.add(new Field("id", "11", Store.NO, Index.NOT_ANALYZED_NO_NORMS));
		doc.add(new Field("text", "bbb", Store.NO, Index.ANALYZED));
		writer.addDocument(doc);*/

		/* This method tries to only load segments that have changed or were created after the IndexReader was (re)opened. */
		val newReader = reader.reopen
		println("两次的Reader是一样的吗？")
		println(reader == newReader)

		reader.close
		indexSearcher = new IndexSearcher(newReader);
		val hits = indexSearcher.search(query, 10);
		println(" 现在的文档数目是多少")
		println(hits.totalHits)
		println(hits.totalHits == 9)

		// confirm new document matched
		val rehits = indexSearcher.search(new TermQuery(new Term("text", "bbb")), 1).totalHits
		println("新增加的文档 能查到吗")
		println(rehits == 1)
		newReader.close
		writer.close
	}

	testNearRealTime

}