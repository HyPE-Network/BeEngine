package org.distril.beengine;

import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public abstract class Tickable extends Thread {

	private final AtomicBoolean running = new AtomicBoolean(true);

	private final float[] tickAverage = new float[20];
	private final float[] useAverage = new float[20];

	private long nextTick, currentTick;

	private float maxTick = 20, maxUse;

	public Tickable(String threadName) {
		super(threadName + " Ticker");
	}

	@Override
	public void run() {
		Arrays.fill(this.tickAverage, 20F);
		Arrays.fill(this.useAverage, 0F);

		this.nextTick = System.currentTimeMillis();
		try {
			while (this.running.get()) {
				try {
					this.tick();

					var next = this.nextTick;
					var current = System.currentTimeMillis();
					if (next - 0.1 > current) {
						var allocated = next - current - 1;
						if (allocated > 0) {
							Thread.sleep(allocated, 900000);
						}
					}
				} catch (RuntimeException exception) {
					log.error("Error whilst ticking server or world", exception);
				}
			}
		} catch (Throwable throwable) {
			log.fatal("Exception happened while ticking server or world", throwable);
		}
	}

	protected abstract void onUpdate(long currentTick);

	private void tick() {
		var tickTime = System.currentTimeMillis();

		// TODO
		var time = tickTime - this.nextTick;
		if (time < -25) {
			try {
				Thread.sleep(Math.max(5, -time - 25));
			} catch (InterruptedException exception) {
				log.error("Server or world interrupted whilst sleeping", exception);
			}
		}

		var tickTimeNano = System.nanoTime();
		if ((tickTime - this.nextTick) < -25) {
			return;
		}

		this.currentTick++;

		this.onUpdate(this.currentTick);

		var nowNano = System.nanoTime();

		var tick = (float) Math.min(20, 1000000000 / Math.max(1000000, ((double) nowNano - tickTimeNano)));
		var use = (float) Math.min(1, ((double) (nowNano - tickTimeNano)) / 50000000);

		if (this.maxTick > tick) {
			this.maxTick = tick;
		}

		if (this.maxUse < use) {
			this.maxUse = use;
		}

		this.copyAverage(this.tickAverage, tick);
		this.copyAverage(this.useAverage, use);

		if ((this.nextTick - tickTime) < -1000) {
			this.nextTick = tickTime;
		} else {
			this.nextTick += 50;
		}
	}

	protected void stopTicking() {
		this.running.set(false);

		this.interrupt();
	}

	private void copyAverage(float[] array, float value) {
		System.arraycopy(array, 1, array, 0, array.length - 1);
		array[array.length - 1] = value;
	}
}
