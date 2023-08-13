package org.distril.beengine.util

import com.google.gson.Gson
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

object Utils {

	val gson = Gson()

	fun createDirectories(first: String, vararg more: String) {
		try {
			val dir = Path.of(first, *more)
			if (!Files.exists(dir)) Files.createDirectories(dir)
		} catch (exception: IOException) {
			throw RuntimeException(exception)
		}
	}

	fun getResource(name: String): InputStream = ClassLoader.getSystemResourceAsStream(name)!!

	fun requireInRange(value: Int, minValue: Int = 0, maxValue: Int, name: String) =
		require(value in minValue until maxValue) { "$name out of bounds" }

	fun Any.getLogger() = LogManager.getLogger(this::class.java)!!
}
