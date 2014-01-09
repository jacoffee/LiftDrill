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
import com.mongodb.ServerAddress
import net.liftweb.mongodb.MongoDB
import com.mongodb.Mongo
import net.liftweb.mongodb.DefaultMongoIdentifier
import com.mongodb.MongoOptions
import net.liftweb.mongodb.MongoAddress
import net.liftweb.mongodb.MongoHost


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Bootable{

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


   // LiftRules.localeCalculator = r => definedLocale.openOr(LiftRules.defaultLocaleCalculator(r))

    // comet clock widget
    LiftRules.cometCreation.append {
      case CometCreationInfo("Clock", name, defaultXml, attributes, session) =>
        new ExampleClock(session, Full("Clock"), name, defaultXml, attributes)
    }

    DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

    initSnippetDisapatch
    // dbBase Connection
    initConnect
  }

  private def initSnippetDisapatch {
	  LiftRules.snippetDispatch.append {
		case "JSoup" => net.liftweb.example.snippet.Scraper
		case "Mongo" => net.liftweb.example.snippet.Mongo
	  }
  }

  private def initConnect {
	  // Properties.whereToLook = () => ((filename, () => Full(new FileInputStream(filename))) :: Nil)
	  val mongoOptions = new MongoOptions
	  mongoOptions.connectionsPerHost = MongoConfig.connectionsPerHost
	  mongoOptions.threadsAllowedToBlockForConnectionMultiplier = MongoConfig.threadsAllowedToBlockForConnectionMultiplier
	  MongoDB.defineDb(
		MongoConfig.DefaultMongoIdentifier,
		MongoAddress(MongoHost(MongoConfig.host, MongoConfig.port, mongoOptions), MongoConfig.db)
	  )
  }

  object MenuInfo {
    def sitemap = SiteMap(
		Menu("Home") / "index" >> Hidden,
		Menu("Stuff") / "interactive",
		Menu("Scraper") / "scraper",
		Menu("Persistence") / "persistence" submenus(
			Menu("XML Fun") / "xml_fun",
			Menu("Database") / "database",
			Menu(Loc("simple", Link(List("simple"), true, "/simple/index"), "Simple Forms") )
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
