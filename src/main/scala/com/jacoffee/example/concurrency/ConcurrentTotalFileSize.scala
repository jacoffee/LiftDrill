package com.jacoffee.example.concurrency

import java.io.File
import scala.collection.mutable.ListBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class ConcurrentTotalFileSize {
	case class SubDirectoriesAndFileSize(longtotalSize: Long,  theSubDirs: ListBuffer[File])  
	// get subdirectories and their size
	def getSubDirectoriesAndSize(file: File) = {
			var len = 0L
			if (file.isDirectory) {
				var subsBuffer = ListBuffer[File]()
				var size = ListBuffer[Long]()
				val subs = file.listFiles.toList
				subs.foreach { 
				  	case dir if dir.isDirectory =>  subsBuffer.append(dir)
				  	case file if file.isFile  => size.append(file.length)
				}
				SubDirectoriesAndFileSize(size.sum, subsBuffer) // 子目录的集合  和 非目录文件的大小 
			} else {
				SubDirectoriesAndFileSize(file.length, ListBuffer())
			}
	} 
	def getTotalSizeOfFilesInDir(file: File):  Long = {
			val service = Executors.newFixedThreadPool(300)
			try {
					val subs = ListBuffer[Future[SubDirectoriesAndFileSize]]()
					file.listFiles.foreach {	dir =>
						 subs.append {
							 service.submit {
								 new Callable[SubDirectoriesAndFileSize]() {
									 def call() = {
										 	getSubDirectoriesAndSize(dir)
									 }
								 }
							 }
						 }
					}
					subs.map(_.get(100, TimeUnit.SECONDS).longtotalSize).sum			  
			} catch {
			 	case e: Exception => { 
			 		e.printStackTrace 
			 		0L
			 	}
			} finally {
				service.shutdown
			}
	}
}


object improvedGet extends App {
	val start = System.nanoTime
	val len = new ConcurrentTotalFileSize().getTotalSizeOfFilesInDir(new File("E:/eclipse64"))
	val end = System.nanoTime
	Thread.sleep(3000)
	println(" size " +  len)
	println(" len " + (end-start)/1.0e9)
}