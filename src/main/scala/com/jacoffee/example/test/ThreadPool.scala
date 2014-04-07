package com.jacoffee.example.test

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.Callable
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions.{ asScalaBuffer, mapAsScalaMap, mapAsJavaMap }
import scala.collection.JavaConverters._

object ThreadPool extends App {
/*	val joinPool = new ForkJoinPool
	val partitions = ListBuffer[Callable[Unit]]()
	(1 to 100).toSet[Int].foreach { num =>  
		partitions.append {
			new Callable[Unit]() {
				override def call = {
					println( s"I am executing ${num}" )
				}
			}
		}
	}
	// invokeAll(Collection<? extends Callable<T>> tasks) 
	val start = System.currentTimeMillis
	joinPool.invokeAll(partitions.asJavaCollection)
	println(" 用时 " + (System.currentTimeMillis-start))*/
	val start = System.currentTimeMillis
	(1 to 100).toList.foreach(println)
	println(" 用时 " + (System.currentTimeMillis-start))
	
}


