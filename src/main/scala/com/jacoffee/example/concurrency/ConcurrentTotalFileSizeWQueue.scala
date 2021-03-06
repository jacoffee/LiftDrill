package com.jacoffee.example.concurrency

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.ArrayBlockingQueue

object ConcurrentTotalFileSizeWQueue extends App {
		val service = Executors.newFixedThreadPool(1000)
		val pendingVisits = new AtomicLong
		// change implementation as BlockQueue
		// put every thread's result in a blockqueue
		val sizeQueue = new ArrayBlockingQueue[Long](500)

		def startExploreFile(file: File) = {
			pendingVisits.incrementAndGet
			service.execute(new Runnable(){
				override def run() {
					exploreDir(file)
				}
			})

		}
		def exploreDir(file: File): Unit = {
		  	var longSize = 0L
			if (file.isFile) {
				longSize = file.length
			} else {
				file.listFiles.foreach {
				  	case dir if dir.isDirectory => {
				  		 service.execute(new Runnable() {
					 		 override def run() = {
					 			 startExploreFile(dir)
					 		 }
				  		 })
				  	}
				  	case file if file.isFile  => longSize = longSize + file.length
				}
			}
			try {
				sizeQueue.put(longSize)
			} catch {
				case e: Exception => println(e.getMessage)
			}
		  	pendingVisits.decrementAndGet
		}


		def getTotalSizeOfFile(file: File) = {
			try {
			 	startExploreFile(file)
			 	var longsize = 0L
			 	while( pendingVisits.get > 0  || sizeQueue.size > 0 ) {
			 		longsize = longsize + sizeQueue.poll(10, TimeUnit.SECONDS)
			 	}
			 	longsize
			}  finally {
				service.shutdown
			}
		}

		val start = System.nanoTime
		val len =	getTotalSizeOfFile(new File("E:/eclipse64"))
		val end = System.nanoTime
		println(" size " +  len)
		println(" len " + (end-start)/1.0e9)
}

