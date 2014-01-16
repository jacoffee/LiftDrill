package net.liftweb.example.snippet

import scala.xml.{ Text, Group, NodeSeq, Elem }
import net.liftweb.example.model.{ Person => PersonModel }
import net.liftweb.util.Helpers.{ strToSuperArrowAssoc, millis, AttrBindParam }
import net.liftweb.util.BindPlus.nodeSeqToBindable
import net.liftweb.http.{ S, SHtml, RequestVar, DispatchSnippet }
import net.liftweb.http.SHtml.ElemAttr
import net.liftweb.http.js.JsCmds.{ Alert, Prompt, SetHtml, jsExpToJsCmd, Noop, Confirm }
import net.liftweb.http.js.jquery.JqJE.{ Jq, JqId, JqRemove }
import net.liftweb.http.js.JE.{ AnonFunc, Call, JsFunc, JsObj, JsArray }
import net.liftweb.http.js.{ JsCmds, JE, JsExp, JsCmd }
import net.liftweb.util.BindPlus.nodeSeqToBindable
import net.liftweb.json.JsonAST.{ JArray, JString, JValue }
import net.liftweb.common.{ Empty, Box, Full }
import net.liftweb.util.{ Helpers, ToJsCmd }
import net.liftweb.http.js.HtmlFixer
import net.liftweb.http.js.JE.ValById
import org.bson.types.ObjectId

object Mongo extends DispatchSnippet {

	def dispatch = {
		case "users" => users
		case "add" => add
		case "call" => call
	}

	private object selectedPerson extends RequestVar[Box[PersonModel]](Empty)

	implicit def stringToNodeSeq(jString: String) = Text(jString)

	case class JsHtml(node: NodeSeq) extends  JsExp with HtmlFixer {
		def toJsCmd = fixHtmlAndJs("inline", node)._1
	}

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
			PersonModel.getAllSortByFirstName.map { person =>
				val idValue = person.id.get.toString
				println("idValue " + idValue)
				<tr>
					<td>{ person.firstName.get }</td>
					<td>{ person.lastName.get }</td>
					<td>{ person.email.get }</td>
					<td>{ PersonModel.formatDate( person.birthDate.get.getTime ) }</td>
					<td>{ person.personalityType.get }</td>
					<td>
						<!-- { SHtml.link("/simple/edit", () => selectedPerson(Full(person)), Text("Edit")) }  -->
						<!--单击编辑 弹出框进行编辑-->
						{
							SHtml.a(
								"edit",
								Call("popupDiv.infoContent",
									"修改Person信息",
									JsHtml {
										<input type="text" value= { idValue + " |||| " + person.firstName.get } id="firstName"></input>
										<input type="text" value= { person.lastName.get } id="lastName"></input>
										<input type="text" value= { person.email.get } id="email"></input>
										<input type="text" value= { PersonModel.formatDate( person.birthDate.get.getTime )} id="date"></input>
									},
									JsObj("确认修改" ->
										AnonFunc(
											// SHtml.jsonCall(jsExpValue, JValue => JsCmd)
											// when clicking the 确认修改按钮 首先获取要传递的数据 并进行验证然后 提交提交到服务器端进行相关处理
											// 最后以JsCmd进行回调处理
											SHtml.jsonCall(
												JsArray{
													List(
														ValById("firstName"),
														ValById("lastName"),
														ValById("email"),
														ValById("date")
													)
												},
												{
													_ match {
														case JArray(
															JString(firstName) :: JString(lastName) :: JString(email) :: JString(date) :: Nil
														) => {
															try {
																//进行更新操作  先查询 然后再进行更新 验证
																PersonModel.find(idValue) match {
																	case Full(person) => {
																		person.firstName(firstName)
																			.lastName(lastName)
																			.email(email)
																			.birthDate(PersonModel.stringToCal(date))
																			.save
																	}
																	case _ =>
																}
																Call("window.location.reload")
															} catch {
																case e: Exception =>  {
																	Call("window.location.reload")
																}
															}
														}
														case _ => {
															Call("window.location.reload")
														}
													}
												}:(JValue => JsCmd)
											)
										)
									)
								).cmd
							)
						}
					</td>
					<td>
						<!-- { SHtml.link("/simple/delete", () => selectedPerson(Full(person)), Text("Delete")) } -->
						<!-- ajaxOperation  -->
						{
							SHtml.a("delete",
								JsCmds.Confirm("确认要删除该记录吗",
									SHtml.ajaxInvoke( () => {
										//  数据删除操作
											PersonModel.deleteUser(idValue)
											Call("window.location.reload")     // jsExpToJsCmd(Call("window.location.reload"))
										}
									).exp
								)
							)
						}
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
				"mycall" -> SHtml.a(Text("add Practice"),  Call("addUser.calculate",JsExp.intToJsExp(3), 4).cmd,  "id" -> "myadd"),
				"delete" -> {
					SHtml.a(
						() => JsCmds.Confirm(
							"确定要删除吗？",
							SHtml.ajaxInvoke(() =>
								{
									S.notice("Operation Completed")
									JsCmds.After(Helpers.TimeSpan(3L), JsCmds.Reload)
								}
							).cmd
						),
						Text("to be deleted")
					)
				},
				"autoExc" -> {
					SHtml.a(
						() => AnonFunc(Alert("ha ha ha")),
						Text("auto exec")
					)
				},
				"action" -> SHtml.hidden(createUser _)
			)
	}

	def call(xhtml: NodeSeq): NodeSeq = {
		SHtml.a(Text("加法练习"),  Call("addUser.calculate",3, 4).cmd,  "id" -> "myadd")
	}

	def createUser = {
		import net.liftweb.mongodb.record.field.Password
		//创建record
		val person = PersonModel.createRecord
		person.firstName(UserName.is)
		person.lastName("haha")
		person.email(Email.is)
		person.password(Pass.is)
		person.birthDate(PersonModel.dateToCal(PersonModel.formatString(DateTime.is)))
		person.save
		saveAndRedirect
	}

	def saveAndRedirect =  {
		S.notice("page_alert", <span>恭喜您， 成功注册</span>)
		S.redirectTo("/simple/index.html")
	}

// 重新实现 SHtml.a
/* def alink (func: () => JsCmd, body: NodeSeq, attrs: ElemAttr*): Elem = {
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
}*/

}
