package net.liftweb.example.record

import net.liftweb.record.Record
import net.liftweb.record.MetaRecord
import net.liftweb.record.field.{StringField, LongField}
import net.liftweb.record.field.BinaryField

object Example extends Example with MetaRecord[Example]

class Example extends Record[Example]{
		
			// class Example needs to be abstract, since 
  // method meta in trait Record of type => net.liftweb.record.MetaRecord[mongo_record.Example] 
            //  is not defined
			
		/**
	   * The meta record (the object that contains the meta result for this type)
	   * def meta: MetaRecord[MyType]
	   */
		def meta  = Example
		
		//  定义一些属性
		// 字符串字段
		 val name = new StringField(this, ""){
				//  from Father TypedField  
				//  def validations: List[ValidationFunction] = Nil
				// type ValidationFunction = ValueType => List[FieldError]
				// 结合上面两端代码  所以 validations 方法应该返回 一个类型 为 FieldError的List
	
				// def valMinLen(len: Int,msg: => String)(value: this.ValueType): List[net.liftweb.util.FieldError]
                override def validations =  valMinLen( 5, "Must be more than 5 characters") _ ::  super.validations
         }
		
		val   funds =  new LongField(this)
}

	object   Entry  extends  App{
         /* Creates a new record */
         
         //def createRecord: BaseRecord =
		Example.createRecord.name ("Tim" ).funds(100000).saveTheRecord
         println( Example.createRecord.name ("Tim" ).funds(100000))
         // class com.qiaobutang.hack.itext.Example={name=Tim, funds=100000, email=Empty}
      
         println( Example.createRecord.name ("tim" ).validate )
         // List(Full(name_id) : Must be more than 5 characters)
      
         println( Example.createRecord.name ("timothy" ).asJSON)
         // {"name": "timothy", "funds": 0, "email": null}
         // 可以直接将一条记录转换成为   JSON对象  def asJSON: net.liftweb.http.js.JsExp
         // 因为在MongoDB中  文件是以 Collection的形式出现的
    }