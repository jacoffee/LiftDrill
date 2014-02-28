package net.liftweb.example.concurrency

object RaceCondition extends App {
	var done = false;
	new Thread(
		new Runnable {
			def run() {
				var i = 0
				while(!done) {
					i = i+1
					println("!Done")
				}
			}
		}
	).start
	// race condition  main Thread and new-launched thread
	println(" OS " + System.getProperty("os.name"))
	//Thread.sleep(2000)
	done = true
	println("flag set to be true")
}

