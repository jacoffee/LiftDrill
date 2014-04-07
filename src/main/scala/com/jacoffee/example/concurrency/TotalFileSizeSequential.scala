package com.jacoffee.example.concurrency

import java.io.File
import scala.collection.mutable.ListBuffer
import java.util.concurrent.Future
import java.util.concurrent.ExecutorService
import java.util.concurrent.Callable
import java.util.concurrent.Executors

// calculate the size of file in certain directory
object TotalFileSizeSequential {
	def getTotalSizeOfFilesInDir(file: File): Long = {
		if (file.isFile) file.length
		else {
			// if it's directory recursively tranverse
			file.listFiles.map(file => getTotalSizeOfFilesInDir(file)).sum
		}
	}

	val start = System.nanoTime
	val len = getTotalSizeOfFilesInDir(new File("D:/Program Files"))
	val end = System.nanoTime
	println(" File Total Size " + len)
	println(" Time Consumed " + (end-start)/1.0e9)
}

object NaivelyConcurrentTotalFileSize extends App {
	def getTotalSizeOfFilesInDir(service: ExecutorService, file: File): Long = {
		if (file.isFile) file.length
		else {
			// if it's directory recursively tranverse
			val parts = ListBuffer[Future[Long]]()
			file.listFiles.foreach{ file =>
				// getTotalSizeOfFilesInDir(file)
				parts.append {
					service.submit(new Callable[Long](){
						override def call = {
							getTotalSizeOfFilesInDir(service, file)
						}
					})
				}
			}
			parts.map(_.get).sum
		}
	}
	// obtain executorservice
	val executor = Executors.newFixedThreadPool(10)
	val start = System.nanoTime
	val len = getTotalSizeOfFilesInDir(executor, new File("D:/Program Files"))
	val end = System.nanoTime
	println(" File Total Size " + len)
	println(" Time Consumed " + (end-start)/1.0e9)
}

