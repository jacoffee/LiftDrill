package net.liftweb.example.snippet

import net.liftweb.http.{SessionVar, SHtml}
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.util.BindPlus.nodeSeqToBindable
import net.liftweb.http.js.JsCmds.{SetHtml, Alert}
import net.liftweb.http.js.jquery.JqJsCmds.Show
import scala.xml.{NodeSeq, Text}



case class Book(reference: String, var title: String)

class MoreAjax {

  //使用SessionVar 模拟数据库
  object stock extends SessionVar[List[Book]](List(
    Book("ABCD", "Harry Potter and the Deathly Hallows"),
    Book("EFGH", "Harry Potter and the Goblet of Fire"),
    Book("IJKL", "Scala in Depth"),
    Book("MNOP", "Lift in Action")))

  // <div id="edit_display" style="display: none;"></div>
  private val editFormDiv = "edit_display"

  def list =
    ".line" #> stock.is.map { b =>
      ".name *" #> b.title & //.name元素的 《内容》  替换成 b.title
        ".name [id]" #> b.reference &
        // 将.name元素的 [id] 属性变成 b.reference
        // 是为了后面的  设置值
        ".edit" #> edit(b)
      // .edit整个 替换为 edit(b)= NodeSeq => NodeSeq
       // def edit(b: net.liftweb.example.snippet.Book): scala.xml.NodeSeq => scala.xml.NodeSeq
    }

  println(list)
  // edit的返回值是一个函数  NodeSeq => NodeSeq 所以ns相当于是函数的参数
  def edit(b: Book): NodeSeq => NodeSeq = { ns: NodeSeq =>
    {
      
      // 这里可以这样理解 它们形成了一种绑定机制 你传入一串<NodeSeq>
      // 然后通过替换 机制就可以 生成另外的 <NodeSeq>
      val form =
        // SHtml.text(getter,setter)
        "#book_name" #> SHtml.text(b.title, b.title = _) &
        // Implicit conversions found: "type=submit" => strToCssBindPromoter("type=submit")
            "type=submit" #> SHtml.ajaxSubmit("Update", 
                () => Alert("you sure")
              ) andThen SHtml.makeFormsAjax
       /*   "type=submit" #> SHtml.ajaxSubmit("Update", () => SetHtml(b.reference, 
              Text(b.title))) andThen SHtml.makeFormsAjax*/
     
      // Vend a function that will take all of the form elements and turns them into Ajax forms    
      // a的默认行为是单击 所以可以省略
       /*
          ns ==
				<p>
					Name: <input id="book_name" />
				</p>
				<p>
					<input type="submit" value="Update" />
				</p>
          */    
      SHtml.a(
        () =>
          // 使用fomr(ns) 内容替换 editFormDiv的内容
          SetHtml(editFormDiv, form(ns)) &
            Show(editFormDiv, 1000),
        Text("Edit"))
    }
  }

}