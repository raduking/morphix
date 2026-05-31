package org.morphix.lang.retry.delay;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.retry.DelayStrategy;

public class FixedDelayStrategy implements DelayStrategy {

	private final long delay;
	private final TimeUnit timeUnit;

	public FixedDelayStrategy(final long delay, final TimeUnit timeUnit) {
		this.delay = delay;
		this.timeUnit = timeUnit;
	}

	public static FixedDelayStrategy of(final long delay, final TimeUnit timeUnit) {
		return new FixedDelayStrategy(delay, timeUnit);
	}

	@Override
	public TimeUnit timeUnit() {
		return timeUnit;
	}

	@Override
	public long delay(final long attempt) {
		if (attempt <= 0) {
			throw new IllegalArgumentException("Attempt must be greater than 0");
		}
		return delay;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final FixedDelayStrategy that = (FixedDelayStrategy) o;
		return delay == that.delay && timeUnit == that.timeUnit;
	}

	@Override
	public int hashCode() {
		return Objects.hash(delay, timeUnit);
	}
}
