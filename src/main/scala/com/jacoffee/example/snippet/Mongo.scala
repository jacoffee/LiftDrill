package com.jacoffee.example.snippet

import scala.xml.{ Text, NodeSeq }
import scala.util.Random
import net.liftweb.example.model.{Person => PersonModel}
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.http._
import net.liftweb.http.js.JsCmds.{ Alert, jsExpToJsCmd, Noop }
import net.liftweb.http.js.JE._
import net.liftweb.http.js.{ HtmlFixer, JsCmds, JsExp, JsCmd }
import net.liftweb.util.BindPlus.nodeSeqToBindable
import net.liftweb.json.JsonAST.{ JArray, JString, JValue }
import net.liftweb.http.js.JE.Call
import net.liftweb.util.Helpers
import net.liftweb.common.Full
import net.liftweb.common.Full
import net.liftweb.http.js.JE.ValById
import net.liftweb.json.JsonAST.JArray
import net.liftweb.http.js.JE.Call
import net.liftweb.json.JsonAST.JString

object Mongo extends DispatchSnippet {

	def dispatch = {
		case "add" => add
		case "users" => users
		case "download" => download
	}

	case class JsHtml(node: NodeSeq) extends JsExp with HtmlFixer {
		def toJsCmd = fixHtmlAndJs("inline", node)._1
	}

	object UserName extends RequestVar[String]("")
	object Email extends RequestVar[String]("")
	object Password extends RequestVar[String]("")
	object Personality extends RequestVar[String]("")

	/* Add a user */
	// "mycall" -> SHtml.a(Text("add Practice"),  Call("addUser.calculate",JsExp.intToJsExp(3), 4).cmd,  "id" -> "myadd")
	def add(xhtml: NodeSeq): NodeSeq = {
		xhtml.bind("input",
			"username" -> SHtml.text(UserName.is, u => UserName(u.trim), "id" -> "username", "class" -> "text", "placeholder" -> "请输入用户名"),
			"email" -> SHtml.text(Email.is, e => Email(e.trim), "id" -> "email", "class" -> "text", "placeholder" -> "请输入邮箱"),
			"password" -> SHtml.text(Password.is, p => Password(p.trim), "id" -> "password", "class" -> "text", "placeholder" -> "请输入密码"),
			"personality" -> SHtml.text(Personality.is, p => Personality(p.trim), "id" -> "personality", "class" -> "text"),
			"delete" -> {
				SHtml.a(
					() => JsCmds.Confirm(
						"确定要删除吗？",
						SHtml.ajaxInvoke(() => {
							S.notice("Operation Completed")
							JsCmds.After(Helpers.TimeSpan(3L), JsCmds.Reload)
						}
						)
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
		person.personalityType(Random.nextInt(3) + 1)
		person.save
		S.redirectTo("/mongo/index", () => S.notice("page_alert", <span>恭喜您， 成功注册</span>))
	}

	/**
	 * Get the XHTML containing a list of users
	 */
	def users(xhtml: NodeSeq): NodeSeq = {
		import net.liftweb.json.DefaultFormats
		implicit val formats = DefaultFormats

		case class MyPerson(username: String, email: String)
		// 查询出所有的Person按 姓氏 升序排列
		val userId = "username"
		val emailId = "email"
		<tr class="tblTitle">
			<th>姓名</th>
			<th>邮箱</th>
			<th>性格</th>
			<th>EDIT</th>
			<th>DELETE</th>
			<th>Mail</th>
		</tr> ++ {
			PersonModel.getAllSortByUsername.map {
				person =>
					val idValue = person.id.get.toString
					val personality = PersonModel.Personality.allTypes(Random.nextInt(3) + 1).toString
					val reload = Call("window.location.reload").cmd
					//需要前提传递Json格式的数据
					def validateAndUpdate(jvalue: JValue): JsCmd = {
						println(" jvalue " + jvalue)
						println(jvalue.extractOpt[MyPerson].isEmpty)
						jvalue.extractOpt[MyPerson].map {
							p =>
								try {
									//进行更新操作  先查询 然后再进行更新 验证
									PersonModel.find(idValue) match {
										case Full(person) => {
											person.username(p.username).email(p.email).save
										}
										case _ =>
									}
									reload
								} catch {
									case e: Exception => reload
								}
						}.getOrElse(reload)
					}

					<tr>
						<td>
							{person.username.get}
						</td>
						<td>
							{person.email.get}
						</td>
						<td>
							{personality}
						</td>
						<td>
						{
							SHtml.a(
								Text("edit"),
								Call("popupDiv.sendInnerMail",
									"修改Person信息",
									JsHtml {
										<input type="text" value={person.username.get} id={userId} name = {person.username.name} />
										<input type="text" value={person.email.get} id={emailId} name={person.email.name}></input>
										<input type="text" value={personality}></input>
										},
										JsObj("确认修改" ->
											AnonFunc(
												// SHtml.jsonCall(jsExpValue, JValue => JsCmd)
												// when clicking the 确认修改按钮 首先获取要传递的数据 并进行验证然后 提交提交到服务器端进行相关处理
												// 最后以JsCmd进行回调处理
												SHtml.jsonCall(Call("sendAsynData.person", ValById(userId), ValById(emailId)), validateAndUpdate _) /*&
														SHtml.jsonCall(
															JsArray {
																List(
																	ValById(userId),
																	ValById(emailId)
																)
															},
															{
																_ match {
																		case JArray( JString(username) :: JString(email) :: Nil) => {
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
																				case e: Exception => {
																					Call("window.location.reload")
																				}
																			}
																		}
																		case _ => {
																			Call("window.location.reload")
																		}
																}
															}:(JValue => JsCmd)
														)*/
											)
										)
									).cmd
								)
							}
							</td>
							<td>
							{
								SHtml.a(
									Text("delete"),
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
						<td>
						{
							SHtml.a(
								Text("发送站内信"),
								Call("popupDiv.sendInnerMail",
									"发送站内信",
									JsHtml {
										<div class="zy-inbox-receiver">
											<label for="username">发给: </label>
											<input type="text" class="zy-form-input" id="to"  name="to" value= { person.username.get }/>
										</div>
										<div class="zy-inbox-content">
											<label for="content">内容: </label>
											<textarea class="send-content" id="content" name="content"></textarea>
										</div>
									},
									JsObj(
										"发送" ->
										AnonFunc(
											SHtml.jsonCall(
												JsArray(
													ValById("to"),
													ValById("content")
												),
												{
													_ match {
														case JArray(JString(to) :: JString(content) :: Nil) =>{
															Call("window.location.reload")
														}
														case _ => Noop
													}
												}: (JValue => JsCmd)
											)
										)
									)
								)
							)
						}
						</td>
					</tr>
			}}
	}

	def download = {
		val poem =
			"Roses are red," ::
			"Violets are blue," ::
			"Lift rocks!" ::
			"And so do you." :: Nil

		def downloadLink =
			SHtml.link(
				"/notused",
				() => throw new ResponseShortcutException(poemTextFile),
				Text("Download")
			)

		def poemTextFile : LiftResponse =
			InMemoryResponse(
				poem.mkString("\n").getBytes("UTF-8"),
				"Content-Type" -> "text/plain; charset=utf8" ::
				"Content-Disposition" -> "attachment; filename=poem.txt" :: Nil, // disposition is very crucial for the download
				cookies=Nil,
				200
			)

		".poem" #> poem.map(line => ".line" #> line) &
		"a" #> downloadLink
	}
}
