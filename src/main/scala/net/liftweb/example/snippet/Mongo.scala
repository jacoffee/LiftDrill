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

object Mongo extends DispatchSnippet{

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

   /* Add a user */
  def add(xhtml: NodeSeq): NodeSeq = {
	<tr>
		<td>
			<a href="/simple/index.html">Cancel</a>
		</td>
		{ selectedPerson.is.openOr(PersonModel.dfltUser) }
		<td>
			<input type="submit" value="Create"/>
		</td>
	</tr>
  }
//openOr(new User).toForm(Empty, saveUser _) ++ <tr>


}
