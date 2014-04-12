package bootstrap.liftweb


import net.liftweb.http._
import js.jquery.JQueryArtifacts
import net.liftweb._
import common._
import common.Full
import db._
import mapper.Schemifier
import sitemap.Loc.{Link, If, Unless, Hidden, ExtLink, MenuCssClass, LocGroup}
import util.{Props, Helpers}
import http._
import actor._
import sitemap._
import Helpers._
import example._
import lib.{AsyncRest, StatelessJson}
import model._
import snippet._
import scala.language.postfixOps
import net.liftmodules.widgets.autocomplete.AutoComplete
import java.sql.{DriverManager, Connection}
import net.liftweb.sitemap.Loc.Test
import com.mongodb.ServerAddress
import net.liftweb.mongodb.MongoDB
import com.mongodb.Mongo
import net.liftweb.mongodb.DefaultMongoIdentifier
import com.mongodb.MongoOptions
import net.liftweb.mongodb.MongoAddress
import net.liftweb.mongodb.MongoHost
import net.liftmodules.widgets.autocomplete.AutoComplete

class Boot extends Bootable{

  def boot {
    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    // 选择默认的 js支持
    //LiftRules.jsArtifacts = JQueryArtifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
    println("》》》》》》》》》》reload in action《《《《《《《《《《《《《")
    LiftRules.setSiteMapFunc(() => MenuInfo.sitemap)
    requestDispatch
    initSnippetDisapatch
    // dbBase Connection
    initConnect
    AutoComplete.init
  }

  private def requestDispatch {
	 // 因为这个相当于是重定向 所以想这个发送请求 相当于请求后面的那个页面 没有state的存储
	LiftRules.statelessRewrite.append {
		case RewriteRequest(ParsePath("formsubmit":: id :: Nil, _, _, _), GetRequest, _) =>
			 RewriteResponse( "formsubmit" :: Nil, Map("id" -> id))
	}
  }

  private def initSnippetDisapatch {
	  LiftRules.snippetDispatch.append {
		case "Scraper" => com.jacoffee.example.snippet.Scraper
		case "Mongo" => com.jacoffee.example.snippet.Mongo
		case "QQMail" => com.jacoffee.example.snippet.QQMail
		case "FormSubmit" => com.jacoffee.example.snippet.FormSubmit
		case "RenderNode" => com.jacoffee.example.snippet.RenderNode
	  }
  }

  private def initConnect {
	  // Properties.whereToLook = () => ((filename, () => Full(new FileInputStream(filename))) :: Nil)
	  val mongoOptions = new MongoOptions
	  mongoOptions.connectionsPerHost = MongoConfig.connectionsPerHost
	  mongoOptions.threadsAllowedToBlockForConnectionMultiplier = MongoConfig.threadsAllowedToBlockForConnectionMultiplier
	  MongoDB.defineDb(
		DefaultMongoIdentifier,
		MongoAddress(MongoHost(MongoConfig.host, MongoConfig.port, mongoOptions), MongoConfig.db)
	  )
  }

  object MenuInfo {
    def sitemap = SiteMap(
		Menu("Home") / "index" >> Hidden,
		// Link的第一个参数和第二个参数 结合就可以打开 整个文件下的文件   最后是当单击菜单上的Crawler时 会跳到哪个菜单
		Menu(Loc("Scraper", Link("crawler" :: Nil, true, "/crawler/scraper"), "Crawler")),
		Menu(Loc("DataBase", Link("mongo" :: Nil, true, "/mongo/"), "DataBase")),
		Menu(Loc("Ajax", Link("ajax" :: Nil, true, "/ajax/formsubmit.html"), "Ajax"))
	)
  }

}
/*

 
     Menu("Interactive Stuff") / "interactive" submenus(
        Menu("Comet Chat") / "chat",
        Menu("Ajax Samples") / "ajax",
        Menu("Ajax Form") / "ajax-form",
        Menu("Modal Dialog") / "rhodeisland",
        Menu("JSON Messaging") / "json",
        Menu("Stateless JSON Messaging") / "stateless_json",
        Menu("More JSON") / "json_more",
        Menu("Ajax and Forms") / "form_ajax"
        )
 
*/