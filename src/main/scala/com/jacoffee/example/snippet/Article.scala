package com.jacoffee.example.snippet

import scala.xml.{Text, Unparsed, Utility, NodeSeq}
import scala.xml.parsing.XhtmlParser
import scala.io.Source
import net.liftweb.http._
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.common.{ Empty, Full }
import net.liftweb.http.js.JsCmds.{ jsExpToJsCmd, Noop }
import com.jacoffee.example.model.{ Article => ArticleModel }
import com.jacoffee.example.util.Helpers.{ isBlank }

/**
 * Created by qbt-allen on 14-4-19.
 */
object Article  extends DispatchSnippet {

	def dispatch = {
		case "searchBox" => searchBox
		case "create" => createArticle
		case "list" => list
	}

	object search extends RequestVar(S.param("search").openOr(""))
	def searchBox = "*" #> SHtml.text(search.is, s => search(s), "placeholder" ->"搜索留言或人...", "name" ->"search", "id" ->"q")

	def toUnparsedSafely(unSafeText: String)(safeTextToUnparsed: String => Option[String]) =
		Option(unSafeText).map(Utility.escape _).flatMap(safeTextToUnparsed).map(Unparsed(_))

	def list = {
		val contentName = ArticleModel.content.name
		val searchedArticles = {
			val q = search.is.trim
			if (q.nonEmpty) ArticleModel.search(contentName, q) else ArticleModel.findAll
		}
		implicit def stringToNode(input: String) = Text(input)
		def highlightText(text: String) = ArticleModel.highlightText(search.is, contentName, text)
		"data-bind=article-records" #> {
			(xhtml: NodeSeq) => {
				searchedArticles.map { article =>
					(
						"data-bind=article-title *" #> article.title.get &
						"data-bind=article-author *" #> article.author.get &
						"data-bind=article-time *" #> ArticleModel.getPublishDate(article.created_at.get) &
						"data-bind=article-content" #> {
							val articleContent = article.content.get
							if (search.is.isEmpty) { <pre>{ articleContent }</pre> }
							else <pre>{ toUnparsedSafely(articleContent)(highlightText _).getOrElse(articleContent: NodeSeq) } </pre>
						} &
						"data-bind=article-action [onclick]" #> {
							SHtml.ajaxInvoke(
								() => {
									ArticleModel.getBoxById(article.idValue).foreach { article =>
										article.like(article.like.get + 1).save(true)
									}
									Noop
								}
							)
						}
					)(xhtml)
				}
			}
		}
	}

	object articleToSave extends RequestVar({
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
			article.save(true)  // after save hook will tigger indexing operation
			S.redirectTo("/zhihu/article")
		}
		val initilaArticle = articleToSave.is

		(xhtml: NodeSeq) => {
			(
				"data-bind=author" #> {
					SHtml.text(initilaArticle.author.get,  a => articleToSave.is.author(a))
				} &
				"data-bind=title" #> {
					SHtml.text(initilaArticle.title.get,  t => articleToSave.is.title(t))
				} &
				"data-bind=content" #> {
					SHtml.textarea(initilaArticle.content.get,  c => articleToSave.is.content(c))
				} &
				"data-bind=comment" #> {
					SHtml.text(initilaArticle.comment.get,  c => articleToSave.is.comment(c))
				} &
				"data-bind=submit-action" #> SHtml.hidden(create _)
			)(xhtml)
		}
	}

	def addLike = {
		import net.liftweb.json.JsonDSL._
		 S.param("like").flatMap{  num =>
			try {
				Some(num.toInt)
			} catch {
				case e: Exception => None
			}
		} match {
			case  Full(num) => Full {
				JsonResponse{
					//("hello", "hello" ) ~ ("you", "ni")
					("updatedLike", (num+1).toString)
				}
			}
			case _ => Empty
		}
	}
}
