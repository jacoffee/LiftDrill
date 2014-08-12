package com.jacoffee.example.comet

import net.liftweb.http.{ CometActor, SHtml, LiftSession }
import net.liftweb.util.Helpers.TimeSpan
import net.liftweb.common.{ Full, Box, Empty }
import scala.xml.{ Text, NodeSeq }
import net.liftweb.actor.LiftActor
import net.liftweb.util.Schedule
import net.liftweb.http.js.JsCmds.{ Run, SetHtml }


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

object Lobby extends LiftActor {
	// 就像QQ大厅一样的  负责管理当前用户 和 管理游戏开局
	// 所以只需要负责 逻辑 而不需要 像PaperScissor那个一样负责页面的渲染
	private var games: List[Game] = Nil
	private var lobby: List[CometActor] = Nil

	def messageHandler = {
		case PairPlayersInLobby => {
			for(i <- 0 until (lobby.size / 2)){
				val players = lobby.take(2)
				val game = new Game(players.head, players.last)
				games ::= game
				players.foreach(_ ! NowPlaying(game))
				lobby = lobby diff players
			}
		}
		case AddPlayer(who) =>{
			println(" Add New Player ")
			lobby ::= who
			this ! PairPlayersInLobby
		}
		case RemovePlayer(who) =>
			lobby = lobby.filter(_ ne who)  // 筛选出不是当前who的用户
	}
}

class Game(playerOne: CometActor, playerTwo: CometActor) extends LiftActor {
	import scala.collection.mutable.Map
	private var moves: Map[CometActor, Box[Move]] = Map()
	clearMoves()

	private def sendToAllPlayers(msg: Any){
		val currentUserNum = moves.map(_._1).toList.size
		println(" currentUserNum " + currentUserNum)
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
					case (Full(Rock), Full(Scissors)) | (Full(Paper), Full(Rock)) | (Full(Scissors), Full(Paper)) => sendToAllPlayers(Winner(playerOne))
					// playerOne didnt win, and its not a tie, so playerTwo must have won
					case _ => sendToAllPlayers(Winner(playerTwo))
				}
			}
			Schedule.schedule(this, ResetGame, TimeSpan(1000L * 5))
		}

		case Make(move, from) => {
			moves.update(from, Full(move))
			if(moves.flatMap(_._2).size == 2) {
				this ! Adjudicate
			} else {
				// one of the players hasnt made their move,
				// prompt the other one to do something
				moves.filter(_._1 ne from).head._1 ! HurryUpAndMakeYourMove
			}
		}
		case ResetGame => {
			clearMoves()
			sendToAllPlayers(ResetGame)
		}
		case LeaveGame =>
		// one player left, you cant play on your own so
		// both players are sent back to the lobby
	}
}

class PaperScissorRock(initSession: LiftSession,
		initType: Box[String],
		initName: Box[String],
		initDefaultXml: NodeSeq,
		initAttributes: Map[String, String])
	extends Comet(initSession, initType, initName, initDefaultXml, initAttributes) {

	println(" initSession " + initSession.uniqueId)
	private var nickName = ""
	private var game: Box[Game] = Empty

	// case class PartialUpdateMsg(cmd: () => JsCmd) extends CometMessage
	private def showInformation(msg: String) = partialUpdate(SetHtml("information", Text(msg)))

	println("xxxxxxxxxxxxx")

	override def mediumPriority = {
		case NowPlaying(g) => {
			game = Full(g)
			reRender(true)
		}
		case HurryUpAndMakeYourMove => showInformation("Hurry up! Your opponent has already made their move!")
		case Tie => showInformation("Damn, it was a tie!")
		case Winner(who) =>
			if(who eq this) showInformation("You are the WINNER!!!")
			else showInformation("Better luck next time, loser!")
		case ResetGame => reRender(true)
	}

	// Note that the render method will be called each time a new browser tab
	// is opened to the comet component or the comet component is otherwise otherwise
	// accessed during a full page load
	def render = {
		val currentListeners = this.cometListeners
		println("当前在线数目 " + currentListeners.size)

		if(!game.isEmpty)
			"#information *" #> "Now you're playing! Make your move..." &
			".line" #> List(Rock, Paper, Scissors).map(move =>
				SHtml.ajaxButton(Text(move.toString), () => {
					game.foreach(_ ! Make(move, this))
					Run("$('button').attr('disabled',true);")
				}))
		else
			"#game *" #> "Waiting in the lobby for an opponent..."
	}
	// override def lifespan: Box[TimeSpan] = Full(2 minutes)

	override def localSetup(){
		println(" Comet Actor Set up !!")
		askUserForNickname
		super.localSetup()
	}
	override def localShutdown() {
		println(" Comet ActorShut Down !!")
		Lobby ! RemovePlayer(this)
		super.localShutdown()
	}

	private def askUserForNickname {
		if (nickName.length == 0){
			ask(new AskName, "What's your nickname?"){
				case s: String if (s.trim.length > 2) =>
					nickName = s.trim
					Lobby ! AddPlayer(this)
					reRender(true)
				case _ =>
					askUserForNickname
					reRender(false)
			}
		}
	}
}

class AskName extends CometActor {
	def render = SHtml.ajaxForm(
		<p>What is your player nickname? <br />
			{
				SHtml.text("",n => answer(n.trim))
			}</p> ++
		<input type="submit" value="Enter Lobby"/>
	)
}
