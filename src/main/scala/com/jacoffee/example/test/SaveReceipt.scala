package com.jacoffee.example.test

import com.jacoffee.example.model.Order

object SaveReceipt extends App {
	// new Boot().initConnect
	// val itemObjectIds = Product.findAll.map(_.id.get)
	// save process
	// Order.user("zml").items(itemObjectIds).save

	
/*	val getOrder = Order.find((Order.user.name, "zml")).map {
		order => order.items.get
	}.map{
		objectids => Product.findAll(objectids)
	}.openOr(Nil)

	getOrder.zipWithIndex.foreach { case (product, index) =>
		println(" 产品编号 " + index)
		println(" 产品名称 " + product.name.get)
		println(" 产品价格 " + product.price.get)
		println(" 产品描述 " + product.desc.get)
	}*/
}