package org.distril.beengine

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.distril.beengine.util.Utils.getLogger
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureNanoTime

abstract class Tickable(threadName: String) : Thread("$threadName Ticker") {

    @Volatile
    private var isRunning = true

    private val tickAverage = FloatArray(20)

    private val useAverage = FloatArray(20)
    private var nextTick = 0L

    private var currentTick = 0L

    private var maxTick = 20f
    private var maxUse = 0f

    override fun run() {
        this.nextTick = System.currentTimeMillis()
        try {
            while (this.isRunning) runBlocking { tick() }
        } catch (throwable: Throwable) {
            log.fatal("Exception happened while in $name", throwable)
        }
    }

    protected abstract suspend fun onUpdate(currentTick: Long)

    private suspend fun tick() = coroutineScope {
        val tickTime = System.currentTimeMillis()

        val time = tickTime - nextTick
        if (time < -25) delay(max(5, -time - 25))

        val usedNanos = measureNanoTime {
            if (tickTime - nextTick < -25) return@coroutineScope

            currentTick++

            onUpdate(currentTick)
        }.toDouble()

        val tick = min(20.0, 1_000_000_000 / max(1_000_000.0, usedNanos)).toFloat()
        val use = min(1.0, usedNanos / 50_000_000).toFloat()

        if (tick > maxTick) maxTick = tick
        tickAverage.copyAverage(tick)

        if (use > maxUse) maxUse = use
        useAverage.copyAverage(use)

        if (nextTick - tickTime < -1000) {
            nextTick = tickTime
        } else {
            nextTick += 50
        }
    }

    protected fun stopTicking() {
        this.isRunning = false
    }

    private fun FloatArray.copyAverage(value: Float) {
        System.arraycopy(this, 1, this, 0, this.size - 1)
        this[this.size - 1] = value
    }

    companion object {

        private val log = Tickable.getLogger()
    }
}
