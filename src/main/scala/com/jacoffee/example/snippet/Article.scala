package com.jacoffee.example.snippet

import scala.xml.{Text, Unparsed, Utility, NodeSeq}
import scala.xml.parsing.XhtmlParser
import scala.io.Source
import scala.concurrent.{ Await, ExecutionContext, Future, future }
import net.liftweb.http._
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftweb.common.{ Empty, Full }
import net.liftweb.http.js.JsCmds.{SetHtml, jsExpToJsCmd, Noop}
import com.jacoffee.example.model.{ Article => ArticleModel }
import com.jacoffee.example.util.Helpers.isBlank
import java.io.{File, FileOutputStream}
import com.jacoffee.example.util.Config.UploadPath
import com.jacoffee.example.util.Config
import net.liftweb.http.js.jquery.JqJE.JqId
import net.liftweb.http.js.JE.Call

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

	// escape &lt; <  &gt; >
	def toUnparsedSafely(unSafeText: String)(safeTextToUnparsed: String => Option[String]) =
		Option(unSafeText).map(Utility.escape _).flatMap(safeTextToUnparsed).map(Unparsed(_))
	implicit def stringToNode(input: String) = Text(input)

	def list = {
		val searchedArticles = {
			val q = search.is.trim
			if (q.isEmpty) ArticleModel.findAll
			else ArticleModel.getByTextSearch(q, Map.empty)
		}

		"data-bind=reindex [onclick]" #> {
			SHtml.ajaxInvoke(
				() => {
					ArticleModel.indexAll
					Noop
				}
			)
		} &
		"data-bind=article-records" #> {
			(xhtml: NodeSeq) => {
				val searchString = search.is

				searchedArticles.map { article =>
					(
						"data-bind=article-title *" #> {
							val title = article.title.get
							if (searchString.isEmpty) title: NodeSeq
							else {
								toUnparsedSafely(title)(
									ArticleModel.createFieldHighlighter(searchString, article.title.name, _)
								).getOrElse(title: NodeSeq)
							}
						}&
						"data-bind=article-author *" #> article.author.get &
						"data-bind=article-time *" #> ArticleModel.getPublishDate(article.created_at.get) &
						"data-bind=article-content *" #> {
							val articleContent = article.content.get
							if (searchString.isEmpty) { <pre>{ articleContent }</pre> }
							else
								<pre>{
									toUnparsedSafely(articleContent)(
										ArticleModel.createFieldHighlighter(searchString, article.content.name, _)
									).getOrElse(articleContent: NodeSeq)
								} </pre>
						} &
						"data-bind=like-num" #> {
							val articleId = article.idValue
							SHtml.a(
								() => {
									val addedLike =
										ArticleModel.getBoxById(articleId).map { article =>
											val likeNum = article.like.get + 1
											article.like(likeNum).save_!
											likeNum
										}.openOr(0)

									SetHtml("article"+articleId.toString, addedLike.toString)
								},
								<lift:children>
									<i class="like-btn"></i>喜欢
									<span class="num" id={ s"article$articleId" }>{ article.like.get }</span>
								</lift:children>
							)
						} &
						"data-bind=delete" #> {
							SHtml.a(
								() => {
									article.delete_!
									Call("window.location.reload")
								},
								"删除"
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

		var fileParamHolder: Option[FileParamHolder] = None

		def create {
			val article = ArticleModel.createRecord
			val articleInReq = articleToSave.is
			article.author(articleInReq.author.get)
			article.title(articleInReq.title.get)
			article.content(articleInReq.content.get)
			article.comment(articleInReq.comment.get)
			// save upload pic in the database
			fileParamHolder.foreach { fp => ArticleModel.saveImage(fp.fileName, fp.file) }
			article.tags(List("政治", "文化", "教育"))
			article.save_!  // after save hook will tigger indexing operation
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
				"data-bind=image" #> {
					SHtml.fileUpload(fp => fileParamHolder = Some(fp))
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
					// ("hello", "hello" ) ~ ("you", "ni")
					("updatedLike", (num+1).toString)
				}
			}
			case _ => Empty
		}
	}
}
