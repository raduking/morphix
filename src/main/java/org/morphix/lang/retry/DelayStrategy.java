package org.morphix.lang.retry;

import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface DelayStrategy {

	long delay(long attempt);

	default TimeUnit timeUnit() {
		return TimeUnit.MILLISECONDS;
	}

	default long delay() {
		return delay(1);
	}

	default long delay(final TimeUnit timeUnit) {
		return timeUnit.convert(delay(), timeUnit());
	}

	default long delay(final int attempt, final TimeUnit timeUnit) {
		return timeUnit.convert(delay(attempt), timeUnit());
	}
}
