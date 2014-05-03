package com.jacoffee.example.concurrency

trait AbstractPrimeCounter {
	def  isPrime(number: Int): Boolean = {
		var primeFlag: Boolean = true
		if(number <=1 )  {
			primeFlag = false 
		} else {
		   (2 to Math.sqrt(number).toInt ).foreach { num => 
			   if( number % num == 0)   primeFlag = false
		   }
		}
		primeFlag
	}
	def countPrimesInRange(lower: Int,  upper: Int) = {
			var count = 0
			println(" entered ??!")
			(lower to upper).foreach{ num => 
				if( isPrime(num) ) count = count + 1
			}
			count
	}
	def timeAndCompute(number: Int) = {
	   val start = System.currentTimeMillis
	   val primeCount  = countPrime(number)
	   val end = System.currentTimeMillis
	   println(" number passed in " + number)
	   println(s"number of prime under ${number} is ${primeCount}")
	   println(s"TimeUsed is ${(end - start)/1000}")
	}

	def countPrime(number: Int): Int  // 返回值很重要  这样才会被认为是重写了
}


class Sequenial extends AbstractPrimeCounter {
	def countPrime(number: Int): Int = countPrimesInRange(1, number)
}

object Calculate extends App  {
		new Sequenial().timeAndCompute(10000000)
}



