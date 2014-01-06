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
import comet.{ThingBuilder, ExampleClock}
import lib.{AsyncRest, StatelessJson}
import model._
import snippet._
import scala.language.postfixOps
import net.liftmodules.widgets.autocomplete.AutoComplete
import java.sql.{DriverManager, Connection}
import net.liftweb.sitemap.Loc.Test


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def boot {
    // where to search snippet
    LiftRules.addToPackages("net.liftweb.example")

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    // 选择默认的 js支持
    LiftRules.jsArtifacts = JQueryArtifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.setSiteMapFunc(() => MenuInfo.sitemap)

    // Dump information about session every 10 seconds
    SessionMaster.sessionWatchers = SessionInfoDumper :: SessionMaster.sessionWatchers

    StatelessJson.init()

    // used by the Ajax example
    AutoComplete.init()

    // used by Misc/LongTime
    ThingBuilder.boot()

    // Async REST sample
    LiftRules.dispatch.append(AsyncRest)


    LiftRules.localeCalculator = r => definedLocale.openOr(LiftRules.defaultLocaleCalculator(r))

    // comet clock widget
    LiftRules.cometCreation.append {
      case CometCreationInfo("Clock", name, defaultXml, attributes, session) =>
        new ExampleClock(session, Full("Clock"), name, defaultXml, attributes)
    }

    DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
    Schemifier.schemify(true, Schemifier.infoF _, Person, User)

  }

  object MenuInfo {
    def sitemap = SiteMap(
      Menu("Home") / "index",
      Menu("BookList") / "book",
      Menu("JsonForm") / "JsonForm",
      Menu("登陆") / "mylogin",
      Menu("Interactive Stuff") / "interactive" submenus(
        Menu("Comet Chat") / "chat",
        Menu("Ajax Samples") / "ajax",
        Menu("Ajax Form") / "ajax-form",
        Menu("Modal Dialog") / "rhodeisland",
        Menu("JSON Messaging") / "json",
        Menu("Stateless JSON Messaging") / "stateless_json",
        Menu("More JSON") / "json_more",
        Menu("Ajax and Forms") / "form_ajax"
        ),
      Menu("Persistence") / "persistence" submenus(
        Menu("XML Fun") / "xml_fun",
        Menu("Database") / "database",
        Menu(Loc("simple", Link(List("simple"), true, "/simple/index"), "Simple Forms")),
        Menu("Templates") / "template"),
      Menu("Templating") / "templating" / "index" submenus(
        Menu("Surround") / "templating" / "surround",
        Menu("Embed") / "templating" / "embed",
        Menu("Evalutation Order") / "templating" / "eval_order",
        Menu("Select <div>s") / "templating" / "selectomatic",
        Menu("Simple Wizard") / "simple_wizard",
        Menu("Lazy Loading") / "lazy",
        Menu("Parallel Snippets") / "parallel",
        Menu("<head/> tag") / "templating" / "head"
        ),
      // 在这种情况下  Web Services菜单一开始 是不能够显示的 只有在满足下面的条件时 才能访问
      // Unless 属于 Control-affeting parameters 
      // and if you don't set the value for id, the request for ws will be redirected to
      //  /index ie. the homepage
        // 如果你这样访问  http://localhost:8080/ws?id=3的时候  就会顺利到 ws页面
      Menu("Web Services") / "ws" >>
      	Unless(
      	() => S.param("username").isEmpty,
      	() => RedirectResponse("http://www.baidu.com")
      	),
      // authentication 参数 只有在  请求满足某种情况下 才能访问
      // 下面的情况就是 只有在是异步请求的时候 才能够访问  guess
      Menu("Localization") / "lang" >>
      Test(req => req.ajax_? == true)
      ,
      Menu("Menus") / "menu" / "index" submenus(
        Menu("First Submenu") / "menu" / "one",
        Menu("Second Submenu (has more)") / "menu" / "two" submenus(
          Menu("First (2) Submenu") / "menu" / "two_one",
          Menu("Second (2) Submenu") / "menu" / "two_two"),
        Menu("Third Submenu") / "menu" / "three",
        Menu("Forth Submenu") / "menu" / "four"
        ),
      Menu("Misc code") / "misc" submenus(
    	//菜单中的结构 和 在 Application的 文件位置是 一一对应的
        Menu("Long Time") / "misc" /"longtime",
        Menu("Number Guessing") / "guess",
        Menu("Wizard") / "wiz",
        Menu("Wizard Challenge") / "wiz2",
        Menu("Simple Screen") / "simple_screen",
        Menu("Variable Screen") / "variable_screen",
        Menu("Arc Challenge #1") / "arc",
        Menu("Simple Wiring") / "simple_wiring",
        Menu("Wiring Invoice") / "invoice_wiring",
        Menu("File Upload") / "file_upload",
        Menu("Async REST") / "async_rest",
        Menu(Loc("login", Link(List("login"), true, "/login/index"),
          <xml:group>Requiring Login<strike>SiteMap</strike> </xml:group>)),
        Menu("Counting") / "count"
        )
    )
  }

  object SessionInfoDumper extends LiftActor with Loggable {
    private var lastTime = millis

    private def cyclePeriod = 1 minute

    import net.liftweb.example.lib.SessionChecker

    protected def messageHandler = {
      case SessionWatcherInfo(sessions) =>
        if ((millis - cyclePeriod) > lastTime) {
          lastTime = millis
          val rt = Runtime.getRuntime
          rt.gc

          RuntimeStats.lastUpdate = now
          RuntimeStats.totalMem = rt.totalMemory
          RuntimeStats.freeMem = rt.freeMemory
          RuntimeStats.sessions = sessions.size

          val percent = (RuntimeStats.freeMem * 100L) / RuntimeStats.totalMem

          // get more aggressive about purging if we're
          // at less than 35% free memory
          if (percent < 35L) {
            SessionChecker.killWhen /= 2L
            if (SessionChecker.killWhen < 5000L)
              SessionChecker.killWhen = 5000L
            SessionChecker.killCnt *= 2
          } else {
            SessionChecker.killWhen *= 2L
            if (SessionChecker.killWhen >
              SessionChecker.defaultKillWhen)
              SessionChecker.killWhen = SessionChecker.defaultKillWhen
            val newKillCnt = SessionChecker.killCnt / 2
            if (newKillCnt > 0) SessionChecker.killCnt = newKillCnt
          }

          def pretty(in: Long): String = if (in > 1000L) pretty(in / 1000L) + "," + (in % 1000L) else in.toString

          val dateStr: String = now.toString
          logger.debug("[MEMDEBUG] At " + dateStr + " Number of open sessions: " + sessions.size)
          logger.debug("[MEMDEBUG] Free Memory: " + pretty(RuntimeStats.freeMem))
          logger.debug("[MEMDEBUG] Total Memory: " + pretty(RuntimeStats.totalMem))
          logger.debug("[MEMDEBUG] Kill Interval: " + (SessionChecker.killWhen / 1000L))
          logger.debug("[MEMDEBUG] Kill Count: " + (SessionChecker.killCnt))
        }
    }
  }

  /**
   * Database connection calculation
   */
  object DBVendor extends ConnectionManager {
    private var pool: List[Connection] = Nil
    private var poolSize = 0
    private val maxPoolSize = 4

    private lazy val chooseDriver = "org.h2.Driver"

    private lazy val chooseURL = "jdbc:h2:mem:lift;DB_CLOSE_DELAY=-1"

    private def createOne: Box[Connection] = {
      try {
        val driverName: String = Props.get("db.driver") openOr chooseDriver
        val dbUrl: String = Props.get("db.path") openOr chooseURL

        Class.forName(driverName)

        val dm = (Props.get("db.user"), Props.get("db.password")) match {
          case (Full(user), Full(pwd)) =>
            DriverManager.getConnection(dbUrl, user, pwd)

          case _ => DriverManager.getConnection(dbUrl)
        }
        Full(dm)
      } catch {
        case e: Exception => e.printStackTrace; Empty
      }
    }

    def newConnection(name: ConnectionIdentifier): Box[Connection] =
      synchronized {
        pool match {
          case Nil if poolSize < maxPoolSize =>
            val ret = createOne
            poolSize = poolSize + 1
            ret.foreach(c => pool = c :: pool)
            ret

          case Nil => wait(1000L); newConnection(name)
          case x :: xs => try {
            x.setAutoCommit(false)
            Full(x)
          } catch {
            case e: Throwable => try {
              pool = xs
              poolSize = poolSize - 1
              x.close
              newConnection(name)
            } catch {
              case e: Throwable => newConnection(name)
            }
          }
        }
      }

    def releaseConnection(conn: Connection): Unit = synchronized {
      pool = conn :: pool
      notify
    }
  }
}
