package com.jacoffee.example.concurrency

import scala.concurrent._
import duration._
import java.util.concurrent.Executors

object InducedLock  extends App {
	val n = Runtime.getRuntime.availableProcessors
	val ecn = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(100))
	//val ecn = ExecutionContext.global
	def sayHiInTheFuture = future { println("Hi!") }(ecn)
	
	 val futures = Range(0,2).map { _ =>
	  future { 
	    try { 
	      // 
	      Await.ready(sayHiInTheFuture, 5 seconds) 
	    } catch {
	      case t: TimeoutException => println("Poo!")
	    }
	  } (ecn)
	}
  
}