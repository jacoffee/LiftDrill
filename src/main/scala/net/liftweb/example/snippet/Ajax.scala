package net.liftweb.example.snippet

import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.util.Helpers._
import js._
import js.jquery._
import JqJsCmds._
import JsCmds._
import SHtml._
import scala.xml.{Text, NodeSeq}
import net.liftmodules.widgets.autocomplete._
import scala.language.postfixOps
import net.liftweb.http.js.JE.{JsNotEq, Num}
import net.liftweb.http.SessionVar

object Ajax extends DispatchSnippet with Loggable {
	def dispatch = {
		case "one" => one
		case "two" => two
		case "delete" => delete
		case "ajaxEdi" => ajaxEdi
	}

	def alert(xhtml: NodeSeq): NodeSeq =  Script(Alert("Important Alert Goes Here!"))

	def one(xhtml: NodeSeq) = SHtml.a(() => Alert("You Click Me"), Text("Go on, Click me "), ("class" -> "mylink"))

	def two(xhtml: NodeSeq): NodeSeq = Script(
			/*SetHtml("replaceme", Text("I have been replaced!")) &
			Alert("Text Replaced")*/
			Run(
				JsIf(JsNotEq(Num(1), Num(2)), Alert("3: 1 does not equal 2!")).toJsCmd
			)
	)

	// Ajax Link
	val elem = <p>nihao</p>
	// def a (func: () ⇒ JsCmd, body: NodeSeq, attrs: ElemAttr*): Elem

	def delete(xhtml: NodeSeq) = {
		SHtml.a(()=>{
			// 单击链接的时候 确认是否删除
			// case class Confirm (text: String, yes: JsCmd) extends JsCmd
			// 第二个参数 很明确的指出了 如果确认的话就怎样
			JsCmds.Confirm("您确定要删除该选项",
				// def ajaxInvoke (func: () ⇒ JsCmd): (String, JsExp)
				SHtml.ajaxInvoke(
					() => {
						// 删除的数据库操作
						S.notice("operation completed")
						// 3秒之后 重新刷新页面
						// case class After(time: TimeSpan, toDo: JsCmd)
						JsCmds.After(3 seconds,JsCmds.Reload)
					}
				).cmd
			)
		}, <div>MADAN</div>)
	}

	object ExampleVar extends SessionVar[String]("Replace me With Text")

	def ajaxEdi(xhtml: NodeSeq): NodeSeq = {
		// SHtml.ajaxEditable(displayContents, editForm, onSubmit)
		// SHtml.text(ExampleVar.is, ExampleVar(_) / whatever you put in the edit form will replace _
		SHtml.ajaxEditable(
				Text(ExampleVar.is),
		//fetch the value from session variable and render it to Text
				SHtml.text(ExampleVar.is, ExampleVar(_)),
		// Here, the input simply displays the value of the user and executes the set function to
		// insert the value back into the session when the OK button,is clicked by the user.
				// def apply(id: String): net.liftweb.http.js.jquery.JqJsCmds.FadeIn
				// 元素id 唯一标志了 一个DOM element 因为name并不是唯一的
				() => FadeIn("test",500,200)
		)
	}

	/*
		<script type="text/javascript">
			<![CDATA[
					if ( 1 != 2 ) { alert("3: 1 does not equal 2!"); };
			]]>
		</script>
	*/
  def render = {
    // local state for the counter
    var cnt = 0

    // build up an ajax <a> tag to increment the counter
    def doClicker(in: NodeSeq) = a(() => {
      cnt = cnt + 1;
      SetHtml("count", Text(cnt.toString))
    }, in)

    // create an ajax select box
    def doSelect(in: NodeSeq) = ajaxSelect((1 to 50).toList.map(i => (i.toString, i.toString)),
      Full(1.toString),
      v => DisplayMessage("messages", ("#number" #> Text(v)).apply(in), 5 seconds, 1 second))

    // build up an ajax text box
    def doText(in: NodeSeq) = ajaxText("", v => DisplayMessage("messages", ("#value" #>
      Text(v)).apply(in), 4 seconds, 1 second))

    // use css selectors to bind the view to the functionality
    "#clicker" #> doClicker _ &
      "#select" #> doSelect _ &
      "#ajaxText" #> doText _
  }

    private def buildQuery(current: String, limit: Int): Seq[String] = {
      logger.info("Checking on server side with " + current + " limit " + limit)
      (1 to limit).map(n => current + "" + n)
    }

  def buttonClick = {
    import js.JE._

    "* [onclick]" #> SHtml.ajaxCall(ValById("the_input"),
      s => SetHtml("messages", <i>Text box is
        {s}
      </i>))
  }
}