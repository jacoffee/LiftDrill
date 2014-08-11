package com.jacoffee.example.comet

import scala.xml.{NodeSeq, Text}
import net.liftweb.http.{LiftSession, CometActor}
import net.liftweb.util.Helpers.{ now, TimeSpan }
import net.liftweb.util.Schedule
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.common.Box
import com.jacoffee.example.util.Helpers

/**
 * Created by qbt-allen on 14-8-9.
 * abstract
 */
abstract class Comet(initSession: LiftSession,
		initType: Box[String],
		initName: Box[String],
		initDefaultXml: NodeSeq,
		initAttributes: Map[String, String]) extends CometActor {
		initCometActor(initSession, initType, initName, initDefaultXml, initAttributes)

		/*
		*  This method will be called after the Actor has started.  Do any setup here.
   		*  DO NOT do initialization in the constructor or in initCometActor... do it here.
		* */
		override protected def localSetup {

			super.localSetup
		}

		// This method will be called as part of the shut-down of the actor.  Release any resources here
		override protected def localShutdown {

			super.localShutdown
		}

}

case object Tick
class ExampleClock(initSession: LiftSession,
		initType: Box[String],
		initName: Box[String],
		initDefaultXml: NodeSeq,
		initAttributes: Map[String, String]) extends Comet(initSession, initType, initName, initDefaultXml, initAttributes) {

	Schedule.schedule(this, Tick, TimeSpan(5 * 1000L))

	def render =
		"#clock_time *" replaceWith Helpers.formatDate(now)

	// def lowPriority: PartialFunction[Any, Unit] = Map.empty
	override def lowPriority = {
		case Tick  => {
			partialUpdate(
				SetHtml(
					"clock_time",
					Text(Helpers.formatDate(now))
				)
			)
			// Schedules the sending of a message to occur after the specified delay.
			// schedule[T](to: SimpleActor[T], msg: T, delay: TimeSpan): ScheduledFuture[Unit] =
			Schedule.schedule(this, Tick, TimeSpan(5 * 1000L))
		}
	}
}
