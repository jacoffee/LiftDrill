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

import _root_.net.liftweb._
import http._
import S._
import util._
import Helpers._

import net.liftweb.example.model._

import _root_.scala.xml.{NodeSeq, Text}

object Template extends DispatchSnippet {
  def dispatch = {
    case "show" => show _
  }

  def show(in: NodeSeq) = {
    {
      val users = User.findAll

      users.length match {
        case 0 => "#hasRecords" #> Text("No records found")
        case _ =>
          "#head-one *" #> "Name" &
          "#head-two *" #> "Email" &
          ".row *" #> (users.map { u =>
            ".name *" #> u.firstName.is &
            ".email *" #> u.email.is
          })
      }
    }.apply(templateFromTemplateAttr openOr Text("Error processing template"))
  }
}
