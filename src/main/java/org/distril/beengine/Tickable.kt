package org.distril.beengine

import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

abstract class Tickable(threadName: String) : Thread("$threadName Ticker") {

	private val running = AtomicBoolean(true)

	private val tickAverage = FloatArray(20)

	private val useAverage = FloatArray(20)
	private var nextTick: Long = 0

	private var currentTick: Long = 0
	private var maxTick = 20f

	private var maxUse = 0f

	init {

		Arrays.fill(this.tickAverage, 20f)
		Arrays.fill(this.useAverage, 0f)
	}

	override fun run() {
		this.nextTick = System.currentTimeMillis()
		try {
			while (this.running.get()) {
				try {
					this.tick()

					val next = this.nextTick
					val current = System.currentTimeMillis()
					if (next - 0.1 > current) {
						val allocated = next - current - 1
						if (allocated > 0) {
							sleep(allocated, 900000)
						}
					}
				} catch (exception: RuntimeException) {
					log.error("Error whilst ticking $name", exception)
				}
			}
		} catch (throwable: Throwable) {
			log.fatal("Exception happened while ticking $name", throwable)
		}
	}

	protected abstract fun onUpdate(currentTick: Long)

	private fun tick() {
		val tickTime = System.currentTimeMillis()

		// TODO
		val time = tickTime - this.nextTick
		if (time < -25) {
			try {
				sleep(max(5, -time - 25))
			} catch (exception: InterruptedException) {
				log.error("$name interrupted whilst sleeping", exception)
			}
		}

		val tickTimeNano = System.nanoTime()
		if (tickTime - this.nextTick < -25) return

		this.currentTick++

		this.onUpdate(currentTick)

		val nowNano = System.nanoTime()
		val tick = min(20.0, 1000000000 / max(1000000.0, nowNano.toDouble() - tickTimeNano)).toFloat()
		val use = min(1.0, (nowNano - tickTimeNano).toDouble() / 50000000).toFloat()

		if (this.maxTick > tick) {
			this.maxTick = tick
		}

		if (this.maxUse < use) {
			this.maxUse = use
		}

		this.tickAverage.copyAverage(tick)
		this.useAverage.copyAverage(use)

		if (this.nextTick - tickTime < -1000) {
			this.nextTick = tickTime
		} else {
			this.nextTick += 50
		}
	}

	protected fun stopTicking() {
		this.running.set(false)
		this.interrupt()
	}

	private fun FloatArray.copyAverage(value: Float) {
		System.arraycopy(this, 1, this, 0, this.size - 1)
		this[this.size - 1] = value
	}

	companion object {

		private val log = LogManager.getLogger(Tickable::class.java)
	}
}
