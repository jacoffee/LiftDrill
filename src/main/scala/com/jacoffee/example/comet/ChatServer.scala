package com.jacoffee.example.comet

import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: Allen
 * Date: 14-6-15
 * Time: 下午4:16
 * To change this template use File | Settings | File Templates.
 */
object ChatServer {
	case class Message(time: Date, user: String, msg: String)
}
