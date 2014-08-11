package bootstrap.liftweb

import net.liftweb.common.Full
import net.liftweb.sitemap.Loc.{ Link, Hidden, LocGroup }
import net.liftweb.util.Props
import net.liftmodules.widgets.autocomplete.AutoComplete
import com.mongodb.MongoOptions
import net.liftweb.mongodb.{ MongoDB, MongoAddress, MongoHost, DefaultMongoIdentifier }
import net.liftweb.http.{ S, PostRequest, ParsePath, GetRequest, RewriteResponse, RewriteRequest, CometCreationInfo, Html5Properties, Req, LiftRules, Bootable }
import net.liftweb.sitemap.{ *, Loc, SiteMap, Menu }
import com.jacoffee.example.util.Config
import com.jacoffee.example.comet.{ ExampleClock, PaperScissorRock }

class Boot extends Bootable {

	def boot {
		//Init the jQuery module, see http://liftweb.net/jquery for more information.
		// 选择默认的 js支持
		//LiftRules.jsArtifacts = JQueryArtifacts
		LiftRules.addToPackages("com.jacoffee.example")
		//Show the spinny image when an Ajax call starts
		LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

		// Make the spinny image go away when it ends
		LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

		// Force the request to be UTF-8
		LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

		// Use HTML5 for rendering
		LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

		// comet clock widget
		LiftRules.cometCreation.append {
			case CometCreationInfo( cometType @ "ExampleClock", name, defaultXml, attributes, session) =>
				new ExampleClock(session, Full(cometType), name, defaultXml, attributes)
			case CometCreationInfo( cometType @ "PaperScissorRock", name, defaultXml, attributes, session) =>
				new PaperScissorRock(session, Full(cometType), name, defaultXml, attributes)
		}

		Props.whereToLook = () =>
			(
			"default.props",
			() => Full(getClass.getClassLoader.getResourceAsStream("props/default.props"))
			):: Nil

		println("》》》》》》》》》》reload in action《《《《《《《《《《《《《")
		LiftRules.setSiteMapFunc(() => MenuInfo.sitemap)
		requestDispatch
		initSnippetDisapatch
		initReqDispatch
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
			case "Default" => com.jacoffee.example.snippet.Default
			case "Scraper" => com.jacoffee.example.snippet.Scraper
			case "Mongo" => com.jacoffee.example.snippet.Mongo
			case "QQMail" => com.jacoffee.example.snippet.QQMail
			case "FormSubmit" => com.jacoffee.example.snippet.FormSubmit
			case "RenderNode" => com.jacoffee.example.snippet.RenderNode
			case "Article" => com.jacoffee.example.snippet.Article
			case "Chat" => com.jacoffee.example.snippet.Chat
		}
	}

	private def initReqDispatch {
		LiftRules.dispatch.append {
			// relative to root dir so the first one is /zhihu/article/like.json req
			// corresponding ajax req => type: "POST", url: "/zhihu/article/like.json",
			case Req("zhihu" :: "article" :: "like" :: Nil, "json", PostRequest) => com.jacoffee.example.snippet.Article.addLike _
		}
	}


	private def initConnect {
		val mongoOptions = new MongoOptions
		mongoOptions.connectionsPerHost = Config.Mongo.connectionsPerHost
		mongoOptions.threadsAllowedToBlockForConnectionMultiplier = Config.Mongo.threadsAllowedToBlockForConnectionMultiplier
		MongoDB.defineDb(
			DefaultMongoIdentifier,
			MongoAddress(MongoHost(Config.Mongo.host, Config.Mongo.port, mongoOptions), Config.Mongo.db)
		)
	}

	object MenuInfo {
		Menu("Static", "Static Content") / "static" / * >> LocGroup("main")
		def sitemap = SiteMap(
			Menu("Home", S ? "Home") / "index" >> Hidden,
			// Link的第一个参数和第二个参数 结合就可以打开 整个文件下的文件   最后是当单击菜单上的Crawler时 会跳到哪个菜单
			Menu(Loc("Scraper", Link("crawler" :: Nil, true, "/crawler/scraper"), "Crawler")),
			Menu(Loc("DataBase", Link("mongo" :: Nil, true, "/mongo/"), "DataBase")) ,
			Menu(Loc("Ajax", Link("ajax" :: Nil, true, "/ajax/formsubmit"), "Ajax")),
			Menu(Loc("Zhihu", Link("zhihu" :: Nil, true, "/zhihu/index"), "Zhihu")),
			Menu(Loc("Design", Link("design" :: Nil, true, "/design/hover"), "Design")),
			Menu(Loc("Chat", Link("chat":: Nil, true, "/chat/"), "Chat"))
		)
	}
}
/*
	上面那种写法 放开目录的方式
	Menu("Static", "Static Content") / "static" / ** >> LocGroup("main") >> loggedIn

The ** method at the end of that definition says that  this definition is applicable for every item below the static directory,
 even for subdirectories.
 **  就是 static 下面的都可以访问 并且static里面的 文件夹的文件夹也可以访问
 If you only want to grant access to one directory level, use *instead.
   static 下面的都可以访问



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