package net.liftweb.example.test

import bootstrap.liftweb.Boot
import net.liftweb.example.model.Food
import net.liftweb.example.model.Fruit
import net.liftweb.common.Full
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.DefaultFormats

object SaveFruit extends App {
	new Boot().initConnect
	// save food then as fruit has one default value so it will be stored alongwith
	// val fruit1 = Fruit.name("longan").price(23.78).origin("Fujian").save
	// val fruit2 = Fruit.name("longan").price(23.78).origin("litchi").save
	import net.liftweb.json.JsonDSL.string2jvalue
	import net.liftweb.json.JsonDSL.pair2Assoc
	import net.liftweb.json.JsonDSL.pair2jvalue
	import net.liftweb.json.Formats

	implicit val format= net.liftweb.json.DefaultFormats

	//JObject( JField("name", "logan") :: JField("name", "logan") :: JField("name", "logan") :: Nil )
	// ("name", "logan") ~ ("price", "23.78") ~ ("orgin", "Fujian")
	val ff = Fruit.create(JObject( JField("name", "logan") :: JField("price", "23.78") :: JField("origin", "582") :: Nil ))
	val ff1 = Fruit.create(JObject( JField("name", "77") :: JField("price", "13.78") :: JField("origin", "ert") :: Nil ))
	Food.category("fresh").sub(ff :: ff1 :: Nil).save
	println("successful save")
}