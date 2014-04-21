package com.jacoffee.example.snippet

import net.liftweb.http.{S, RequestVar, SHtml, DispatchSnippet}
import net.liftweb.util.Helpers.strToCssBindPromoter
import com.jacoffee.example.model.{ Article => ArticleModel }
import scala.xml.NodeSeq

/**
 * Created by qbt-allen on 14-4-19.
 */
object Article  extends DispatchSnippet {
	def dispatch = {
		case "searchBox" => searchBox
		case "create" => create
		case "list" => list
	}

	object search extends RequestVar(S.param("search").openOr(""))
	def searchBox = "*" #> SHtml.text(search.is, s => search(s), "placeholder" ->"搜索留言或人...", "name" ->"search", "id" ->"q")
	def list = {
		val searchedArticles = {
			val q = search.is.trim
			if (q.nonEmpty) ArticleModel.search("content", search.is) else ArticleModel.findAll
		}
		"data-bind=article-records" #> {
			(xhtml: NodeSeq) => {
				searchedArticles.map { article =>
					(
						"data-bind=article-title *" #>  { article.title.get } &
						"data-bind=article-author *" #>  { article.author.get } &
						"data-bind=article-time *" #>  { ArticleModel.getPublishDate(article.created_at.get) } &
						"data-bind=article-content" #> { article.content.get }
					)(xhtml)
				}
			}
		}
	}

	object articleToSave extends RequestVar({
		ArticleModel.createRecord
	})
	def create = {

		def create {
			val article = ArticleModel.createRecord
			val articleInReq = articleToSave.is
			article.author(articleInReq.author.get)
			article.title(articleInReq.title.get)
			article.content(articleInReq.content.get)
			article.comment(articleInReq.comment.get)
			article.tags(List("政治", "文化", "教育"))
			article.save(true)

			// index the article
			ArticleModel.indexArticle(article)
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
					SHtml.text(initilaArticle.content.get,  c => articleToSave.is.content(c))
				} &
				"data-bind=comment" #> {
					SHtml.text(initilaArticle.comment.get,  c => articleToSave.is.comment(c))
				} &
				"data-bind=submit-action" #> SHtml.hidden(create _)
			)(xhtml)
		}
	}

}
