package net.liftweb.example.contact

import javax.script.ScriptEngineManager
import java.io.FileReader

object JavaInvokeJS extends App {
	val factory = new ScriptEngineManager(); 
	val engine = factory.getEngineByName("JavaScript"); 
	
	engine.eval("var a=3; var b=4;print (a+b);")
	engine.eval(new FileReader("C:\\Users\\qbt\\Desktop\\qmlogin1b3654.js"))
	engine.eval("var t=checkInput('');");
	val p=engine.get("p").toString();
}