/*
 * Copyright 2007-2013 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb.example.snippet

import _root_.scala.xml._
import _root_.net.liftweb._
import http._
import S._
import SHtml._
import common._
import util._
import Helpers._
import js._
import JsCmds._

class JsonForm {
		import net.liftweb.builtin.snippet.CSS
		def head = Script(json.jsCmd) 
		
		// net.liftweb.http.js.JsCmds.Run
		
		//def jQuery = SHtml.a(() => , body, attrs)
		// scala.xml.NodeSeq
		// 默认调用 json的 apply方法 
		def show = "#form" #> ((ns: NodeSeq) =>  SHtml.jsonForm(json, ns))

		//def show(xhtml: NodeSeq): NodeSeq = xhtml
		object json extends JsonHandler {
				def apply(in: Any): JsCmd =
						SetHtml("json_result", in match {
							case JsonCmd("processForm", _, params: Map[String, _], _) => 
							  	<p>Publisher: { params("publisher") },  Title: { params("title") }</p>
							case x =>
								<span class="error">Unknown error: {x}</span>
						})
		}
		
}
