package net.liftweb.example.snippet

import net.liftweb._
import http._
import mapper.{Ascending, OrderBy}
import S._
import SHtml._
import common._
import util._
import net.liftweb.example.model._
import Helpers._
import _root_.java.util.Locale
import xml.{Text, Group, NodeSeq}
import net.liftweb.example.model.{ Person => PersonModel }
import net.liftweb.util.Helpers.{ strToSuperArrowAssoc, millis, AttrBindParam }
import net.liftweb.util.BindPlus.nodeSeqToBindable
import net.liftweb.http.js.JsCmds.Alert
import net.liftweb.http.js.JsCmd
import scala.xml.Elem
import net.liftweb.http.js.JE
import net.liftweb.http.js.JE.AnonFunc
import net.liftweb.http.js.JE.Call
import net.liftweb.http.js.JsCmds.Prompt
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.ToJsCmd

object Mongo extends DispatchSnippet with SHtml {

	def dispatch = {
		case "users" => users
		case "add" => add
		//case "upload" => upload
	}

	private object selectedPerson extends RequestVar[Box[PersonModel]](Empty)

	/**
	 * Get the XHTML containing a list of users
	 */
	def users(xhtml: NodeSeq): NodeSeq = {
		// 查询出所有的Person按 姓氏 升序排列
		// the header
		<tr class="tblTitle">
			{
				PersonModel.listFieldsName.map { field =>
					<th>{ field.toUpperCase }</th>
				}
			}
			<th>EDIT</th>
			<th>DELETE</th>
		</tr> ++
		{
			Person.getAllSortByFirstName.map {person =>
				<tr>
					<td>{ person.firstName.get }</td>
					<td>{ person.lastName.get }</td>
					<td>{ person.email.get }</td>
					<td>{ PersonModel.formatDate( person.birthDate.get.getTime() ) }</td>
					<td>{ person.personalityType.get }</td>
					<td>
						{ link("/simple/edit", () => selectedPerson(Full(person)), Text("Edit")) }
					</td>
					<td>
						{ link("/simple/delete", () => selectedPerson(Full(person)), Text("Delete")) }
					</td>
				</tr>
			}
		}
	}

	object UserName extends RequestVar[String]("")
	object Email extends RequestVar[String]("")
	object Pass extends RequestVar[String]("")
	object DateTime extends RequestVar[String]("")
   /* Add a user */
	def add(xhtml: NodeSeq): NodeSeq = {
			xhtml.bind("input",
				"username" -> SHtml.text(UserName.is, u => UserName(u.trim), "id"-> "username", "class"-> "text",
						"placeholder" -> "请输入用户名"),
				"email" -> SHtml.text(Email.is, e =>  Email(e.trim), "id" -> "email", "class" -> "text",
						"placeholder" -> "请输入邮箱"),
				"password" -> SHtml.password(Pass.is, p => Pass(p.trim), "id" -> "password", "class" -> "text",
						"placeholder" -> "请输入密码"),
				"datetime" -> SHtml.text(DateTime.is, d => DateTime(d.trim), "id" -> "dateTime", "class" -> "text",
						"placeholder" -> "请输入日期"),
				"mycall" ->  SHtml.a(Text("I am calling"),  Call("addUser.overPopup").cmd,  "id" -> "mycall"),
				"action" -> SHtml.hidden(createUser _)
			)
	}

	def createUser = {
		import net.liftweb.mongodb.record.field.Password
		//创建record
		val person = Person.createRecord
		person.firstName(UserName.is)
		person.lastName("haha")
		person.email(Email.is)
		person.password(Password(Pass.is))
		person.birthDate(PersonModel.dateToCal(PersonModel.formatString(DateTime.is)))
		person.save
		saveAndRedirect
	}

	def saveAndRedirect =  {
		println("success ---------------------------------")
		S.notice("page_alert", <span>恭喜您， 成功注册</span>)
		S.redirectTo("/simple/index.html")
	}

	// 重新实现 SHtml.a
	def alink (func: () => JsCmd, body: NodeSeq, attrs: ElemAttr*): Elem = {
		       attrs.foldLeft(
                // fmapFunc 给每一个函数 建立 map 映射
                  fmapFunc( (func)  )
                      ( name =>
                          <a href="javascript://" onclick={
                                makeAjaxCall( JE.Str(name + "=true")   ).toJsCmd +   "; return false;"
                          }>
                              { s"${name}${body}" }
                          </a>
                      )
        )(_ % _)
	}

}
