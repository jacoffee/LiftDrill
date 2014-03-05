package net.liftweb.example.DivideAndConquer

import java.util.concurrent.Executor
import java.util.ArrayDeque

class ExecutorTest extends Executor {
	override def execute(command: Runnable) = {
		// 将Runnable的任务丢进去之后 会执行它的run方法
		command.run
		// More typically, tasks are executed in some thread other than the caller's thread. 
		// The executor below spawns a new thread for each task
		/*
		 * new Thread(command).start 
		 */
	}
}

object ExecutorTest extends ExecutorTest with App {
	execute(new Runnable(){
		override def run {
			println("Execution !!!")
		}
	})
}

// Many Executor implementations impose some sort of limitation on how and when tasks are scheduled. 
// Executor could be intepretated as "调度器"

// 由于作为调度器的存在 所以它使用了很多线程管理的东西 
class SerialExecutor(executor: Executor) extends Executor {
   val tasks = new ArrayDeque[Runnable]
   var active: Runnable = null

  def execute(r: Runnable) {
	 // Inserts the specified element at the end of this deque.
     tasks.offer(new Runnable() {
       def run {
         try {
           r.run
         } finally {
           scheduleNext
         }
       }
     })
     if (active == null) {
       scheduleNext
     }
   }

   protected def scheduleNext = {
	 // the head of the queue represented by this deque
	 var active = tasks.poll  
     if (active != null) {
       executor.execute(active)
     }
   }
 }
