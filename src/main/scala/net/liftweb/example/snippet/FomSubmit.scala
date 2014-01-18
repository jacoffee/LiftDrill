/*
 * Copyright 2010-2013 WorldWide Conferencing, LLC
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

import net.liftweb.http.{SHtml,SessionVar,DispatchSnippet, S}
import scala.xml.NodeSeq
import net.liftweb.common.{Box, Empty}
import net.liftweb.util.Helpers._
import net.liftweb.http.rest.RestHelper

/*
 * object BasicDispatchUsage extends RestHelper {
	serve {
		// 在地址栏输入信息即可访问    thingToResp(b>Static</b>)
		//case "my" :: "sample" :: _ Get _ => <b>Static</b>
		//
	  	case "sample" :: "one" :: _ XmlGet _ => <b>Static</b>
	}
}*/
// 不要放到别的对象里面去了
object LoggedIn extends SessionVar[Box[String]](Empty)

object FormSubmit extends DispatchSnippet{
  
		def dispatch = {
			  case "checkLogIn"   =>  checkLogIn _
			  case _  => render _
		}
		
		
		def checkLogIn(xhtml: NodeSeq): NodeSeq = {
				 transfer(xhtml) 
		}
		
	// The CSS selector functionality in Lift gives you a CssSelfunction, which is  NodeSeq => NodeSeq
	 val transfer = "name=username"  #> SHtml.text("", 
				    username => LoggedIn(Box.!!(username))) &
				    "type=submit"  #> SHtml.onSubmitUnit(process)
				    
	  def process() = {
	      S.notice(<p>登陆成功</p>)
	      S.redirectTo("/index")
	   }
	
	  def render(xhtml: NodeSeq): NodeSeq  =  {
				<div>This snippet evaluated on 
				{
					  Thread.currentThread.getName
				} 
			  </div>
		}
}