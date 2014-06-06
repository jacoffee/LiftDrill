package com.jacoffee.example.util

/**
 * Created by qbt-allen on 14-6-6.
 */
class Cache {

}

class TempCache[V](cacheCycle: Long)(getValue: => V) {
	@volatile var cache: Option[(Long, V)] = None
	def get = {
		cache match {
			case Some((time, v)) if (cacheCycle + time > System.currentTimeMillis) => {
				cache = Some(System.currentTimeMillis -> v)
				v
			}
			case _ => {
				val compute = getValue
				cache = Some(System.currentTimeMillis, compute)
				compute
			}
		}
	}
}
