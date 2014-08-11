package com.jacoffee.example.comet

import net.liftweb.http.{ CometActor, SHtml, LiftSession }
import net.liftweb.util.Helpers.TimeSpan
import net.liftweb.common.{ Full, Box, Empty }
import scala.xml.{ Text, NodeSeq }
import net.liftweb.actor.LiftActor
import net.liftweb.util.Schedule
import net.liftweb.http.js.JsCmds.{Run, SetHtml}

/**
 * Created by qbt-allen on 14-8-11.
 */

sealed trait Move
final case object Rock extends Move
final case object Paper extends Move
final case object Scissors extends Move

sealed trait Outcome
final case object Tie
final case class Winner(is: CometActor)

final case class AddPlayer(who: CometActor)
final case class RemovePlayer(who: CometActor)
final case object PairPlayersInLobby
final case class NowPlaying(game: Game)
final case class Make(move: Move, from: CometActor)
final case object HurryUpAndMakeYourMove
final case object ResetGame
final case object LeaveGame
final case object Adjudicate

// comet actor usually in charge of  partial update page
// while lift actor simply process task
object Lobby extends LiftActor {

	private var lobby: List[CometActor] = Nil
	private var games: List[Game] = Nil

	def messageHandler = {
		case PairPlayersInLobby => {
			println("XXXXXXXXXXXXXXX")
			for (i <- 0 until (lobby.size/2)) {
				println(" lobby.size/2  ")
				// 每次取最新的两个人 match
				val players = lobby.take(2)
				val game = new Game(players.head, players.last)
				games = game :: games
				players.foreach(_ ! NowPlaying(game))
				lobby diff players // 剔除已经 参赛的 Actor
			}

		}
		case AddPlayer(who) => {
			lobby = who :: lobby
			// 添加一个参赛者 之后 要立即帮它配对
			this ! PairPlayersInLobby
		}
		case RemovePlayer(who) => {
			lobby = lobby.filter(_ ne who)
		}
	}
}

class Game(playerOne: CometActor, playerTwo: CometActor) extends LiftActor {
	private var moves: Map[CometActor, Box[Move]] = Map()
	// clearMoves()

	private def sendToAllPlayers(msg: Any){
		moves.foreach(_._1 ! msg)
	}

	private def clearMoves() {
		moves = Map(playerOne -> Empty, playerTwo -> Empty)
	}

	def messageHandler = {
		case Adjudicate => {
			val p1move = moves(playerOne)
			val p2move = moves(playerTwo)
			if(p1move == p2move)
				sendToAllPlayers(Tie)
			else {
				(p1move, p2move) match {
					case (Full(Rock), Full(Scissors)) |
					     (Full(Paper), Full(Rock)) |
					     (Full(Scissors), Full(Paper)) =>
						sendToAllPlayers(Winner(playerOne))
					case _ =>
						// playerOne didnt win, and its not a tie, so playerTwo must have won
						sendToAllPlayers(Winner(playerTwo))
				}
			}
			Schedule.schedule(this, ResetGame, TimeSpan(5 * 1000L))
		}

		case Make(move, from) => {
			moves.updated(from,Full(move))
			println(" moves " + moves)
			println(" Move Maded !!!")
			if(moves.flatMap(_._2).size == 2){
				this ! Adjudicate
			} else {
				println(" This Circle !!!")
				// one of the players hasnt made their move,
				// prompt the other one to do something
				moves.filter(_._1 ne from).head._1 ! HurryUpAndMakeYourMove
			}
		}
		case ResetGame =>
			clearMoves()
			sendToAllPlayers(ResetGame)

		case LeaveGame =>
		// one player left, you cant play on your own so
		// both players are sent back to the lobby

	}
}

class PaperScissorRock(initSession: LiftSession,
		initType: Box[String],
		initName: Box[String],
		initDefaultXml: NodeSeq,
		initAttributes: Map[String, String]) extends Comet(initSession, initType, initName, initDefaultXml, initAttributes) {

	private var nickname = ""
	private var game: Box[Game] = Empty

	override def localSetup {
		askUserForNickname
		super.localSetup
	}
	override def localShutdown {
		Lobby ! RemovePlayer(this)
		super.localShutdown
	}

	private def showInformation(msg: String) =
		partialUpdate(SetHtml("information", Text(msg)))

	def render =
		if(!game.isEmpty)
			"#information *" #> "Now you're playing! Make your move..." &
			".line" #> {
				List(Rock, Paper, Scissors).map(move =>
					SHtml.ajaxButton(
						Text(move.toString),
						() => {
							game.foreach(_ ! Make(move, this))
							Run("$('button').attr('disabled',true);")
						}
					)
				)
			}
		else
			"#game *" #> "Waiting in the lobby for an opponent..."

	override def lifespan: Box[TimeSpan] = Full(TimeSpan(1000L * 60 * 2))

	override def mediumPriority = {
		case NowPlaying(g) => {
			game = Full(g)
			reRender(true)
		}
		case HurryUpAndMakeYourMove =>
			showInformation("Hurry up! Your opponent has already made their move!")
		case Tie =>
			showInformation("Damn, it was a tie!")
		case Winner(who) =>
			if(who eq this) showInformation("You are the WINNER!!!")
			else showInformation("Better luck next time, loser!")
		case ResetGame => reRender(true)
	}

	private def askUserForNickname {
		if (nickname.isEmpty) {
			ask(new AskName(initSession, initType, initName, initDefaultXml, initAttributes), "what's your nick name") {
				case s: String if (s.trim.length > 2)=> {
					nickname = s.trim
					Lobby ! AddPlayer(this)
					reRender(false)
				}
				case _ => {
					askUserForNickname
					reRender(false)
				}
			}

		}
	}
}

class AskName(initSession: LiftSession,
		 initType: Box[String],
		initName: Box[String],
		initDefaultXml: NodeSeq,
		initAttributes: Map[String, String]) extends Comet(initSession, initType, initName, initDefaultXml, initAttributes) {

		def render = SHtml.ajaxForm(
			<p>
				What is your player nickname? <br />
				{
					SHtml.text("",n => answer(n.trim))
				}
			</p> ++
			<input type="submit" value="Enter Lobby"/>
		)
}
