package com.jacoffee.example.snippet

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
import scala.util.Random

object Mongo extends DispatchSnippet {

	def dispatch = {
		case "users" => users
		case "add" => add
	}

	private object selectedPerson extends RequestVar[Box[PersonModel]](Empty)

	case class JsHtml(node: NodeSeq) extends  JsExp with HtmlFixer {
		def toJsCmd = fixHtmlAndJs("inline", node)._1
	}

	/**
	 * Get the XHTML containing a list of users
	 */
	def users(xhtml: NodeSeq): NodeSeq = {
		// 查询出所有的Person按 姓氏 升序排列
		// the header
		val userId = "username"
		val emailId = "email"
		val personalityId = "personality"
		<tr class="tblTitle">
			<th>姓名</th>
			<th>邮箱</th>
			<th>性格</th>
			<th>EDIT</th>
			<th>DELETE</th>
		</tr> ++
		{
			PersonModel.getAllSortByUsername.map { person =>
				val idValue = person.id.get.toString
				val personality = PersonModel.Personality.allTypes(Random.nextInt(3)+1).toString
				<tr>
					<td>{ person.username.get }</td>
					<td>{ person.email.get }</td>
					<td>{ personality }</td>
					<td>
						{
							SHtml.a(
								Text("edit"),
								Call("popupDiv.infoContent",
									"修改Person信息",
									JsHtml {
										<input type="text" value= { person.username.get } id={ userId }></input>
										<input type="text" value= { person.email.get } id={ emailId }></input>
										<input type="text" value= { personality } id={ personalityId }></input>
									},
									JsObj("确认修改" ->
										AnonFunc(
											// SHtml.jsonCall(jsExpValue, JValue => JsCmd)
											// when clicking the 确认修改按钮 首先获取要传递的数据 并进行验证然后 提交提交到服务器端进行相关处理
											// 最后以JsCmd进行回调处理
											SHtml.jsonCall(
												JsArray{
													List(
														ValById(userId),
														ValById(emailId)
													)
												},
												{
													_ match {
														case JArray(
															JString(username) :: JString(email) :: Nil
														) => {
															try {
																//进行更新操作  先查询 然后再进行更新 验证
																PersonModel.find(idValue) match {
																	case Full(person) => {
																		person.username(username)
																			.email(email)
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
							SHtml.a(Text("delete"),
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
	object Password extends RequestVar[String]("")
	object Personality extends RequestVar[String]("")
	/* Add a user */
	// "mycall" -> SHtml.a(Text("add Practice"),  Call("addUser.calculate",JsExp.intToJsExp(3), 4).cmd,  "id" -> "myadd")
	def add(xhtml: NodeSeq): NodeSeq = {
		xhtml.bind("input",
			"username" -> SHtml.text(UserName.is, u => UserName(u.trim), "id"-> "username", "class"-> "text", "placeholder" -> "请输入用户名"),
			"email" -> SHtml.text(Email.is, e =>  Email(e.trim), "id" -> "email", "class" -> "text", "placeholder" -> "请输入邮箱"),
			"password" -> SHtml.text(Password.is, p =>  Password(p.trim), "id" -> "password", "class" -> "text", "placeholder" -> "请输入密码"),
			"personality" -> SHtml.text(Personality.is, p => Personality(p.trim), "id" -> "personality", "class" -> "text"),
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
			"action" -> SHtml.hidden(createUser _)
		)
	}

	def createUser = {
		//创建record
		val person = PersonModel.createRecord
		person.username(UserName.is)
		person.email(Email.is)
		person.password(Password.is)
		person.personalityType(Random.nextInt(3)+1)
		person.save
		S.redirectTo("/mongo/index", () => S.notice("page_alert", <span>恭喜您， 成功注册</span>))
	}
}
