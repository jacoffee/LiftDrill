package com.jacoffee.example.concurrency

import java.io.File
import scala.collection.mutable.ListBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.CountDownLatch

object ConcurrentTotalFileSizeWLatch extends App {
		val service = Executors.newFixedThreadPool(1000)
		val pendingVisits = new AtomicLong
		val totalSize = new AtomicLong
		val latch = new CountDownLatch(1)

		// 借助原子性的操作来更新
		def updateTotalSizeOfFilesInDir(file: File): Unit = {
		  var longSize = 0L
			if (file.isFile) {
				longSize = file.length
			} else {
				pendingVisits.incrementAndGet
				file.listFiles.foreach {
				  	case dir if dir.isDirectory => {
				  		 service.execute(new Runnable() {
					 		 override def run() = {
					 			 updateTotalSizeOfFilesInDir(dir)
					 		 }
				  		 })
				  	}
				  	case file if file.isFile  => longSize = longSize + file.length
				}
			}
		  	totalSize.addAndGet(longSize)
		  	if (pendingVisits.decrementAndGet == 0) latch.countDown 
		}
		def getTotalSizeOfFile(file: File) = {
			pendingVisits.incrementAndGet
			try {
				updateTotalSizeOfFilesInDir(file)
				// latch.await(10, TimeUnit.SECONDS)
				Thread.sleep(1000)
				totalSize.longValue
/*
  Causes the current thread to wait until the latch has counted down to
     zero, unless the thread is {@linkplain Thread#interrupt interrupted},
     or the specified waiting time elapses. 
 
 * */
			} catch {
				  case e: Exception => {
					  e.printStackTrace
					  0L
			  } 
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

