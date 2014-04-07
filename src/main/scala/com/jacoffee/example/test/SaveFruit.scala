package com.jacoffee.example.test

import bootstrap.liftweb.Boot
import net.liftweb.example.model.{ Food, Fruit }
import net.liftweb.common.Full
import net.liftweb.mongodb.Upsert
import net.liftweb.json.JsonDSL.string2jvalue
import net.liftweb.json.JsonDSL.pair2jvalue
import net.liftweb.json.Formats
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonAST.JField

object SaveFruit extends App {
	// new Boot().initConnect
	// save food then as fruit has one default value so it will be stored alongwith
	// val fruit1 = Fruit.name("longan").price(23.78).origin("Fujian").save
	// val fruit2 = Fruit.name("longan").price(23.78).origin("litchi").save
	implicit val format= net.liftweb.json.DefaultFormats
	//JObject( JField("name", "logan") :: JField("name", "logan") :: JField("name", "logan") :: Nil )
	// ("name", "logan") ~ ("price", "23.78") ~ ("orgin", "Fujian")
	val ff = Fruit.create(JObject( JField("name", "logan") :: JField("price", "23.78") :: JField("origin", "582") :: Nil ))
	val ff1 = Fruit.create(JObject( JField("name", "77") :: JField("price", "13.78") :: JField("origin", "ert") :: Nil ))
	// Food.category("fresh").sub(ff :: ff1 :: Nil).save

	// 更新其中一个文件  定位更新
	// Food.update(Food.category.name -> "fresh", { "$set" -> {"sub.0.price" -> "100.00"} }, Upsert)
	// update embeded document field
	// Food.update(Food.category.name -> "fresh", { "$set" -> {"sub.0.name" -> "sherlock"} }, Upsert)
	// push a new kind of fruit $push // $pull just do the opposite
	import net.liftweb.util.Helpers.strToSuperArrowAssoc
	// val subJ = ("name" , "logan") ~ ("price" , "23.78") ~ ("origin", "ThaiLand")
	val mainJ: JObject = (Food.category.name, "fresh")
	val subJ: JObject = "sub" -> JObject( JField("name", "logan") :: JField("price", "logan") :: JField("origin", "logan") :: Nil )
	Food.update(
		Food.category.name -> "fresh",
		{ ("$pull", "sub" -> JObject( JField("name", "sherlock") ::  Nil )) }: JObject,
		Upsert
	)
	// delete
	// pull one of the fruit
	// delete the whole document
	// Full(class net.liftweb.example.model.Food={_id=5316bab37c1f2a9577e4a727, sub=List(Fruit(logan,100.00,582), Fruit(77,13.78,ert)), category=fresh})
	// println( Food.find("sub.name" -> "logan") ) // sub.name为logan的文档 document
	println("successful save")
}