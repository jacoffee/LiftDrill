package com.jacoffee.example.concurrency

import java.io.File
import java.util.concurrent.{ ExecutorService, Future, Callable, Executors, TimeUnit }
import scala.collection.mutable.ListBuffer 


object TotalFileSize extends App {
	var len = 0L 
	def getTotalSizeOfFilesInDirSequential(file: File): Long = {
		 if (file.isFile)  len =  len + file.length
		 else { 
			 file.listFiles.foreach(getTotalSizeOfFilesInDirSequential)
		  }
		 len
	}

	var sizeBuffer = 0
	def getTotalSizeOfFilesInDirConcurrent(service: ExecutorService, file: File): Long = {
			// service is for thread management and scheduling
			if (file.isFile)   {
				println(" Thread IN File " + Thread.currentThread.getName )
				println(" file Name" + file.getName)
				file.length 
			} else {
				// partitons to be assgined task
				val partsToReceive = ListBuffer[Future[Long]]()
				file.listFiles.foreach { child => 
				  	// For each file or subdirectory, we create a task for finding its size and schedule it on the pool 
				  	// each task will be assgined to a thread, if  thread get one cal done, it will be idle and waited to be assgined again
					partsToReceive.append {
// Submits a value-returning task for execution and returns a Future representing the pending results of the task
						service.submit {
							println(" 堵塞中..............b")
							new Callable[Long] ()  {  // 如果没有线程可以用 这个方法就会堵塞  
								 println(" Thread  assigned " + Thread.currentThread.getName )
								override def call()= {
									  println(" Thread  In Call  " + Thread.currentThread.getName )
									 getTotalSizeOfFilesInDirConcurrent(service, child)
								}
						 }
						}
					}
				}
				println(" partsToReceive num   " + partsToReceive.size )
				0L
				// partsToReceive.map(part => part.get(100, TimeUnit.SECONDS)).sum
				/*
				 * 
While threads wait for response from the tasks they create,
these tasks end up waiting in the ExecutorService’s queue for their turn to run
也许真正发生堵塞的地方是上面这行代码
				 */
			}
	}

	def calculateSeq(file: File)  = {
		val start = System.nanoTime
		val len = getTotalSizeOfFilesInDirSequential(file) 
		val end =System.nanoTime
		println(" time  seq " + (end-start)/1.0e9 )
		println(" size  " + len)
	}

	def  calculateCon(service: ExecutorService, file: File) = {
		val start = System.nanoTime
		val len = getTotalSizeOfFilesInDirConcurrent(service, file) 
		val end =System.nanoTime
		println(" time  con" + (end-start)/1.0e9 )
		println(" size  " + len)
	}
	//	 calculateSeq(new File("E:/eclipse64"))
	// val execservice = Executors.newFixedThreadPool(1)
	try {
		// for concurrent method if thread are not enough, it will result in  pool induced deadlock
/*
While threads wait for response from the tasks they create,
these tasks end up waiting in the ExecutorService’s queue for their turn to run.

* */
		 // calculateCon(execservice, new File("E:/22"))
		 //Thread.sleep(2000)
		 calculateSeq(new File("E:/eclipse64"))
	} finally {
		//execservice.shutdown  
		/*
		 * 
			Thread  assigned main
		   Thread  assigned pool-1-thread-1
 			Thread  In Call  pool-1-thread-1
 			Thread IN File pool-1-thread-1
 			file Name壁纸.jpg
 			但只有一个线程可用的时候  service给 壁纸执行分配了一个线程 但是其它的两个文件夹 没有任务分配
 			在shutdown 会执行所有已分配的任务 并且不再接受新任务 所以此时另外两个文件夹 根本没有执行
 			
		*/
	}
}