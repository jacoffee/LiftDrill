package net.liftweb.example

import net.liftweb.http.rest.RestHelper

case class Book(publisher: String, title: String) 

object  BookShop{
	val stock = List(
			Book("Bloomsbury", "Harry Potter and the Deathly Hallows"),
			Book("Bloomsbury", "Harry Potter and the Goblet of Fire"),
			Book("Manning", "Scala in Depth"),
			Book("Manning", "Lift in Action")
	)
}

object BasicDispatchUsage extends RestHelper {
		serve {
				case "bookshop" :: "books" :: publisher ::Nil XmlGet _ => 
				  response(BookShop.stock.filter( _.publisher equalsIgnoreCase publisher) )
		}
		// 调用的时候  要注意写法
		// http://localhost:8080/bookshop/books/manning.xml
		
		//被上面的Serve调用  所以可是设置为私有
		private def response(in: List[Book])  = {
				<books>
						{
								in.flatMap(
										book => <book   publisher={book.publisher}  title={book.title}  />
								)
						}
				</books>
		}
		
}

/*

private def response(in: List[Book]) = 
			<books>
					{
						in.flatMap(b =>
							<book publisher={b.publisher} title={b.title}/>)
					}
			</books>

*/


