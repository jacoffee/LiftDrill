package com.jacoffee.example.concurrency

import java.util.concurrent.locks.ReentrantLock
import java.util.Arrays
import java.util.concurrent.TimeUnit
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveAction
import java.util.concurrent.RecursiveTask
import java.util.concurrent.ForkJoinTask
import scala.collection.mutable.ListBuffer
import  scala.collection.JavaConverters
import  scala.collection.JavaConversions.mutableSeqAsJavaList
import java.util.concurrent.Callable
import java.util.concurrent.Future

class Account(var balance: Double, var name: String)  extends Comparable[Account] {
	val lock = new ReentrantLock
	def compareTo(other: Account) = {
			hashCode.compareTo(other.hashCode)
	}
	def withDraw(amount: Double): Boolean = {
			lock.lock
			try {
				if( amount > balance) false
				else {
					// FIXME 
					balance = balance - amount
					true
				} 
			}  finally {
				lock.unlock
			}
	}
	def deposit(amount: Double): Boolean = {
			lock.lock
			try {
				if (amount > 0) { balance = balance + amount; true }
				else false
			} finally {
				lock.unlock
			}
	}

}

object AccountService {
  def moneyTransfer(from: Account, to: Account, amount: Double) {
	 	val arr = Array[Account](from, to)
		println(" arr " + arr.toList.map(_.name))
		println(" Thread Inside " + Thread.currentThread.getName )
	    if(arr(0).lock.tryLock(1, TimeUnit.SECONDS))  {
	    	try {
	    		if(arr(1).lock.tryLock(1, TimeUnit.SECONDS)) {
	    			try {
	    				if(arr(0).withDraw(amount)) {
	    				   to.deposit(amount)
	    				   println(s"${arr(0).name}向${arr(1).name}转账${amount}")  
	    				} else  println(s"${arr(0).name}向${arr(1).name}转账失败")
	    			} finally {
	    				arr(1).lock.unlock
	    			}
	    		} else println("转账失败2")
	    	} finally {
	    	  arr(0).lock.unlock
	    	} 
	    }  else {
	     	println("Unable to acquire locks on the accounts ")
	    }
}
}


object transfer extends App {
		// Fork task and Join the result together
		val pool = new ForkJoinPool
		val tasks = ListBuffer[Callable[Unit]]()
		val acc1 = new Account(1000, "zml")
		val acc2 = new Account(1000, "xiaoling")
			tasks.append(
				new Callable[Unit]() {
				  println(" Thread " + Thread.currentThread.getName )
				  def call = AccountService.moneyTransfer(acc1, acc2, 300)
				},
				new Callable[Unit]() {
				  // 任务分配不会执行
				  println(" Thread " + Thread.currentThread.getName )
				  def call = AccountService.moneyTransfer(acc2, acc1, 400)
				}
			)
		pool.invokeAll({ 
			println(" -----------------------------")
			  println(" InvokeAll " + Thread.currentThread.getName )
			  println(" XXXXXXXXXXXXXXXXX")
		  tasks 
		 })
		println("此时 各自的余额为")
		println(" zml " + acc1.balance)
		println(" xiaoling " + acc2.balance)
		//pool.invoke

} 