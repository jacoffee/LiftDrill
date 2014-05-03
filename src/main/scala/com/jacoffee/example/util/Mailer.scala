package com.jacoffee.example.util

import scala.language.postfixOps
import net.liftweb.util.{ Mailer => LiftMailer }
import net.liftweb.util.Mailer.{ From, To, Subject, PlainMailBodyType }
import net.liftweb.util.Schedule
import net.liftweb.util.Helpers._

/**
 * Created by qbt-allen on 14-4-23.
 * for Mailer
 */
object Mailer extends App {

	// you can not send a mail with local server
/*
	LiftMailer.sendMail(
		From("361541673@qq.com"),
		Subject("Hello"),
		To("361541673@163.com"),
		PlainMailBodyType("Hello from Lift")
	)
*/

	// catch a glimpse of Scheduler -- running task a moment later
	// actually thread pool scheduling
	Schedule.schedule( () =>
		{
			println(" Thread Name not Main Thread I guess " + Thread.currentThread.getName)
			println(" I am learing Scheduler")
		},
		3 seconds
	)
	Schedule.shutdown
	// Thread.sleep(3000) why this can not work ??? waited to be figured out
}



