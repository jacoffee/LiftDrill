package com.jacoffee.example.snippet

import net.liftweb.http.{ RequestVar, SHtml, DispatchSnippet }
import net.liftweb.util.Helpers.strToCssBindPromoter
import com.jacoffee.example.model.{ Article => ArticleModel }
import scala.xml.NodeSeq

/**
 * Created by qbt-allen on 14-4-19.
 */
object Article  extends DispatchSnippet {
	def dispatch = {
		case "create" => createArticle
		case "list" => articleList
	}

	def articleList = {

		"*" #> <p></p>
	}

	object articleToSave extends RequestVar({
		println("XXXXXXXXX")
		ArticleModel.createRecord
	})
	def createArticle = {

		def create {
			val article = ArticleModel.createRecord
			val articleInReq = articleToSave.is
			article.author(articleInReq.author.get)
			article.title(articleInReq.title.get)
			article.content(articleInReq.content.get)
			article.comment(articleInReq.comment.get)
			article.tags(List("政治", "文化", "教育"))
			article.save(true)
		}

		(xhtml: NodeSeq) => {
			(
			"data-bind=article-field" #> {
				ArticleModel.getFields.map { field =>
					<div>
						<label for={ field.uniqueFieldId.openOr("") }>{ field.fieldLabel }</label>
						{
							SHtml.text(field.get, inputValue => { println("验证执行流程");articleToSave.is.author(inputValue) }, "class" ->{ field.name }, "maxlength" -> { field.maxLength.toString })
						}
					</div>
				}
			} &
			"data-bind=submit-action" #> SHtml.hidden(create _)
			)(xhtml)
		}
	}

}
