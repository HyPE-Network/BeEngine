package org.distril.beengine.util

import kotlin.system.measureTimeMillis

object MethodWatcher {

    inline fun watch(name: String, block: () -> Unit) = println("$name took ${measureTimeMillis(block)} ms!")
}
