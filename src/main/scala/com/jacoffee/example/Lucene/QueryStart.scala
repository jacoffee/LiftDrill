package com.jacoffee.example.Lucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.util.Version
import org.apache.lucene.document.{ Document, Fieldable, Field }
import org.apache.lucene.document.Field
import org.apache.lucene.document.Field.{ Store, Index }
import java.io.File
import scala.io.Source
import org.apache.lucene.document.Field.TermVector
import scala.collection.mutable.ArrayBuffer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriter.MaxFieldLength
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.queryParser.MultiFieldQueryParser
import org.apache.lucene.search.Filter
import org.apache.lucene.search.TermQuery
import org.apache.lucene.index.Term
import org.apache.lucene.search.MatchAllDocsQuery

class LuceneStorage {
	// 数据源地址
	val srcPath = "D:\\index\\usb.txt"
	// 分词器
	val analyzer = new StandardAnalyzer(Version.LUCENE_34)
	// To store an index on disk, use this instead:
	val config = new IndexWriterConfig(Version.LUCENE_34, analyzer);
	// index place 存放索引文件的位置，即索引库
	val indexedFilePosition = FSDirectory.open(new File("D:/luceneindex"))
	
	
	// 创建索引  实际上就是创建了一个 Lucene 数据库
	// 使用 IndexWriter来增删查改
	def createIndex = {
		val file = new File(srcPath)
		// Documents are the unit of indexing and search. A Document is a set of fields. Each field has a name and a textual value. A field may be stored with the document, in which case it is returned with search hits on the document. 
		// Thus each document should typically contain one or more stored fields which uniquely identify it.
		// 估计因为这个Document  field/Value 对  使的  Mongo的使用更有可能  虽然肯定不局限于key/value pair
		
		val document = new Document
		//   添加文件名 到索引
		//   public Field(String name, String value, Store store, Index index, TermVector termVector) {
		// >> Enum Store 
		//   Store the original field value in the index. This is useful for short texts
		// like a document's title 
		document.add(new Field("filename", file.getName, Store.YES, Index.ANALYZED))
		//  检索到的内容
		document.add(new Field("content", readFileContent(srcPath), Store.YES, Index.ANALYZED));
		//  文件大小
		//document.add(new Field("size", file.length.toString, Store.YES, Index.NOT_ANALYZED))
		// 检索文件的位置
		document.add(new Field("position", file.getAbsolutePath, Store.YES, Index.NOT_ANALYZED))

		// build index
		// An IndexWriter creates and maintains an index.
		val indexWriter = new IndexWriter(indexedFilePosition, config)
		
		indexWriter.addDocument(document)
		// 完成索引添加
		indexWriter.close
	}

	// 文档搜索
	def search = {
		// object DirectoryReader in package index cannot be accessed in package 
		val queryString = "filename:usb.txt"  //精确匹配   filename 和 content都会匹配
		
		// 把要搜索的文本解析成Query
		val fields = Array("filename","content")
		// val queryParser = new MultiFieldQueryParser(Version.LUCENE_34, fields, analyzer);
		// val query = queryParser.parse(queryString)

		// begin to search  find from index base
		val iSearcher = new IndexSearcher(indexedFilePosition)
		// val filter = new Filter
		//  A Query that matches documents containing a term
		val query = new TermQuery(new Term("filename", "usb.txt"))
		val topDocs = iSearcher.search(query, 10)
		println("总共有【" + topDocs.totalHits + "】条匹配结果");
		for(scoreDoc <- topDocs.scoreDocs){
			// 文档内部编号
			val index = scoreDoc.doc
			// 根据编号取出文档
			val doc = iSearcher.doc(scoreDoc.doc)
			println("content "+ doc.get("filename"))
			println("file size "+ doc.get("position"))
		}
	}
	
	
	def readFileContent(path: String): String = {
		Source.fromFile(path).getLines.map(_.replaceAll("\n","")).mkString
	}
	
}


object QueryStart extends App {
	val lucene = new LuceneStorage
	lucene.createIndex
	lucene.search
}