package org.distril.beengine.util

import com.google.gson.Gson
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

	fun getResource(name: String): InputStream = Utils::class.java.classLoader.getResourceAsStream(name)!!
}
