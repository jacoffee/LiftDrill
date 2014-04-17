package com.jacoffee.example.divide_conquer

import java.net.URL
import java.io.{ InputStreamReader, BufferedReader }
import java.text.DecimalFormat
import java.util.concurrent.{ ExecutorService, Callable, Executors }
import scala.io.Source
import scala.collection.mutable.{ HashMap => HMap}
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions.{ asScalaBuffer, mapAsScalaMap, mapAsJavaMap }
import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit

class YahooFinance {
	// http://finance.yahoo.com/echarts?s=CSV+Interactive
	// fetch the price of certain stock like BIDU/SINA/YaHOO In Certain Day
	def getPrice(ticker: String): Double = {
		val fromURL = s"http://ichart.finance.yahoo.com/table.csv?s=${ticker}&a=01&b=28&c=2014&d=01&e=28&f=2014&ignore=.csv"
		val stockDetail = Source.fromURL(fromURL).getLines
		val stockDetailTitle = stockDetail.next
		stockDetail.next.split(",").toList.lastOption.getOrElse("170.56").toDouble
	}
}

trait StockInfo {
	// http://finance.yahoo.com/echarts?s=CSV+Interactive
	// fetch the price of certain stock like BIDU/SINA/YaHOO In Certain Day
	def getPrice(ticker: String): Double = {
		val fromURL = s"http://ichart.finance.yahoo.com/table.csv?s=${ticker}&a=01&b=28&c=2014&d=01&e=28&f=2014&ignore=.csv"
		val stockDetail = Source.fromURL(fromURL).getLines
		val stockDetailTitle = stockDetail.next
		stockDetail.next.split(",").toList.lastOption.getOrElse("170.56").toDouble
	}
	def extractStockInfo(stock: String) = {
		val pattern = """(\p{Upper}+),(\d+)""".r // (p{Upper}+),(d+) java.util.regex.PatternSyntaxException:
		pattern findFirstMatchIn stock match {
			case Some(stockInfo) => (stockInfo.group(1), stockInfo.group(2).toDouble)
			case _ => ("BIDU", "4455".toDouble)
		}
	}
	def getStockInfoMap = {
		val stockInfoMap = HMap[String, Double]()
		val filePath = System.getProperty("user.dir") + System.getProperty("file.separator") + "src\\main\\webapp\\Concurrency\\divideAndConquer\\stocks.txt"
		//Concurrency/divideAndConquer/stocks.txt
		Source.fromFile(filePath).getLines.foreach { stockList =>
			extractStockInfo(stockList) match {
				case (stockName, quantity) => stockInfoMap += stockName -> quantity
			}
		}
		stockInfoMap.toMap
	}
	// 计算净资产
	def timeAndComputeValue = {
		val start = System.nanoTime
		// obtain Stock Name
		val nav = computeNetAssetValue(getStockInfoMap)
		val end = System.nanoTime
		val formattedValue = new DecimalFormat("$##,##0.00").format(nav);
		println("Your net asset value is " + formattedValue);
		println("Time (seconds) taken " + (end - start)/1.0e9); // 10 的 9次方
	}
	def computeNetAssetValue(stockMap: Map[String, Double]):Double;
} 

class SequentialNAV extends StockInfo {
	// if you have to explicitly override "father" method write it
	// 计算手里所持有的股票的总价值 Volume * Price
	override def computeNetAssetValue(stockMap: Map[String, Double]) = {
		var netAssetValue = 0.0
		stockMap.foreach { 
			case (stockName, quantity) => netAssetValue += getPrice(stockName) * quantity
		}
		netAssetValue
	}

	def getCalculationTime = timeAndComputeValue
}

class ConcurrentNAV extends StockInfo {
	
	override def computeNetAssetValue(stockMap: Map[String, Double]) = {
		// Caculate Threads needed for task execution
		val numOfProcessors = Runtime.getRuntime.availableProcessors
		val blockCoefficient = 0.9
		//val poolSize =  (numOfProcessors/ (1- blockCoefficient)).toInt
		val poolSize = 100 //  
		// If  there  are  more  divisions  than  the  pool  size,  they  get queued  for  their  execution  turn
		println(" 处理器数目  " + numOfProcessors)
		println(" 处理任务所需要的线程数目 " + poolSize)
		
		
		// Callable 对象  由于NAV的计算值返回的是 Double 所以这个地方也需要返回Double
		// Callable 可以形象的理解为 待执行的任务集合
		val splitParts = ListBuffer[Callable[Double]]()
		var netAssetValue = 0.0
		// 原来是一个线程  一个股票一个股票去计算  现在改成 每一只股票 一个线程分别去计算 然后汇总在一起
		stockMap.foreach { 
			case (stockName, quantity) => { // division standard  stock symbol
				splitParts.append{
					// this is accordance with the principle of isolated mutability
					new Callable[Double]() {
						println(" thread Name " + Thread.currentThread.getName )
						override def call = {
							getPrice(stockName) * quantity
						}
					}
				}
			}
		}
		//  The threads in the pool will exist until it is explicitly
		val executorPool = Executors.newFixedThreadPool(poolSize)
		// execute all the  calculation tasks  together
		// 把所有的计算任务分解成 若干个 然后将每一个任务计算的结果 进行汇总  因为Callble[Double]  
		// val valueOfStock: java.util.List[java.util.concurrent.Future[Double]]
		val valueOfStock = executorPool.invokeAll(splitParts.asJava, 10000, TimeUnit.SECONDS)
		netAssetValue = valueOfStock.map(_.get).sum
		// 这个地方等到 全部计算完毕之后 才进行汇总 还可以继续优化
		executorPool.shutdown
		netAssetValue
	}

	def getCalculationTime = timeAndComputeValue
}

object fetchStock extends App {
	val snav = new SequentialNAV
	val cnav = new ConcurrentNAV
	snav.getCalculationTime
	cnav.getCalculationTime
}