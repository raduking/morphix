/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.lang.accumulator;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.lang.Temporals;
import org.morphix.lang.thread.Threads;

/**
 * Test class for {@link DurationAccumulator}.
 *
 * @author Radu Sebastian LAZIN
 */
class DurationAccumulatorTest {

	private static final String OK = "OK";
	private static final String FAIL = "FAIL";
	private static final String DEFAULT = "DEFAULT";

	private DurationAccumulator accumulator;

	@BeforeEach
	void setUp() {
		accumulator = DurationAccumulator.of();
	}

	@Test
	void shouldAccumulateDurationWhenRunnableIsExecuted() {
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(50)));

		List<Double> durations = accumulator.durationsAsDouble();
		assertThat(durations, hasSize(1));
		assertThat(durations.getFirst(), greaterThan(0.0));
	}

	@Test
	void shouldReturnSupplierResultAndAccumulateDuration() {
		String result = accumulator.accumulate(() -> {
			Threads.safeSleep(Duration.ofMillis(50));
			return OK;
		}, () -> FAIL);

		assertThat(result, is(OK));
		List<Double> durations = accumulator.durationsAsDouble();
		assertThat(durations, hasSize(1));
		assertThat(durations.getFirst(), greaterThan(0.0));
	}

	@Test
	void shouldReturnDefaultValueIfSupplierThrows() {
		Exception exception = null;

		try {
			accumulator.accumulate(() -> {
				throw new RuntimeException(FAIL);
			}, () -> DEFAULT);
		} catch (Exception e) {
			exception = e;
		}

		assertNotNull(exception);
		assertThat(exception.getMessage(), equalTo(FAIL));
		List<Double> durations = accumulator.durationsAsDouble();
		assertThat(durations, hasSize(1));
	}

	@Test
	void shouldReturnDefaultValueForAverageWhenNoDurationsAccumulated() {
		double avg = accumulator.average();
		assertThat(avg, is(Double.NaN));
	}

	@Test
	void shouldReturnCorrectAverageForAccumulatedDurations() {
		accumulator.getInformationList().add(Duration.ofMillis(10));
		accumulator.getInformationList().add(Duration.ofMillis(20));
		accumulator.getInformationList().add(Duration.ofMillis(30));

		double avg = accumulator.average();
		assertThat(avg, equalTo(0.02));
	}

	@Test
	void shouldReturnDefaultValueForMaxWhenNoDurationsAccumulated() {
		double max = accumulator.max();
		assertThat(max, is(Double.NaN));
	}

	@Test
	void shouldReturnCorrectMaxForAccumulatedDurations() {
		accumulator.getInformationList().add(Duration.ofMillis(10));
		accumulator.getInformationList().add(Duration.ofMillis(25));
		accumulator.getInformationList().add(Duration.ofMillis(15));

		double max = accumulator.max();
		assertThat(max, equalTo(0.025));
	}

	@Test
	void shouldReturnNaNForMinWhenNoDurationsAccumulated() {
		double max = accumulator.min();
		assertThat(max, equalTo(Double.NaN));
	}

	@Test
	void shouldReturnCorrectMinForAccumulatedDurations() {
		accumulator.getInformationList().add(Duration.ofMillis(15));
		accumulator.getInformationList().add(Duration.ofMillis(35));
		accumulator.getInformationList().add(Duration.ofMillis(25));

		double min = accumulator.min();
		assertThat(min, equalTo(0.015));
	}

	@Test
	void shouldGeneratePercentile90() {
		DurationAccumulator durationAccumulator = DurationAccumulator.of();

		durationAccumulator.accumulate(() -> false);

		durationAccumulator.buildStatistics();
		var statistics = durationAccumulator.getStatistics();

		assertThat(statistics.getP90RequestTime(), equalTo(Temporals.toDouble(durationAccumulator.getInformationList().getFirst())));
	}

	@Test
	void shouldReturnDefaultValueForEmptyDurations() {
		List<Double> emptyList = List.of();
		double result = DurationAccumulator.percentile(emptyList, 95.0);
		assertThat(result, equalTo(Double.NaN));
	}

	@Test
	void shouldReturnCorrectPercentileFromList() {
		List<Double> durations = List.of(0.1, 0.2, 0.3, 0.4, 0.5);
		double p50 = DurationAccumulator.percentile(durations, 50.0);
		double p90 = DurationAccumulator.percentile(durations, 90.0);
		double p100 = DurationAccumulator.percentile(durations, 100.0);

		assertThat(p50, equalTo(0.3));
		assertThat(p90, equalTo(0.5));
		assertThat(p100, equalTo(0.5));
	}

	@Test
	void shouldReturnCorrectPercentileFromAccumulatedDurations() {
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(10)));
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(20)));
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(30)));
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(40)));

		double p25 = accumulator.percentile(25.0);
		double p50 = accumulator.percentile(50.0);
		double p75 = accumulator.percentile(75.0);
		double p100 = accumulator.percentile(100.0);

		assertThat(p25, closeTo(0.015, 0.01)); // ~15ms
		assertThat(p50, closeTo(0.025, 0.01)); // ~25ms
		assertThat(p75, closeTo(0.035, 0.01)); // ~35ms
		assertThat(p100, closeTo(0.04, 0.01)); // ~40ms
	}

	@Test
	void shouldReturnNaNForMedianWhenNoDurationsAccumulated() {
		double median = accumulator.median();
		assertThat(median, equalTo(Double.NaN));
	}

	@Test
	void shouldReturnCorrectMedianForAccumulatedDurations() {
		accumulator.getInformationList().add(Duration.ofMillis(10));
		accumulator.getInformationList().add(Duration.ofMillis(20));
		accumulator.getInformationList().add(Duration.ofMillis(30));
		accumulator.getInformationList().add(Duration.ofMillis(40));

		double median = accumulator.median();
		assertThat(median, equalTo(0.02));
	}

	@Test
	void shouldReturnCorrectMedianForOddNumberOfDurations() {
		accumulator.getInformationList().add(Duration.ofMillis(50));
		accumulator.getInformationList().add(Duration.ofMillis(60));
		accumulator.getInformationList().add(Duration.ofMillis(70));

		double median = accumulator.median();
		assertThat(median, equalTo(0.06));
	}

	@Test
	void shouldReturnNaNForPercentileWhenNoDurationsAccumulated() {
		double p95 = accumulator.percentile(95.0);
		double p50 = accumulator.percentile(50.0);

		assertThat(p95, equalTo(Double.NaN));
		assertThat(p50, equalTo(Double.NaN));
	}

	@Test
	void shouldReturnCorrectPercentilesForOddNumberOfDurations() {
		accumulator.getInformationList().add(Duration.ofMillis(10));
		accumulator.getInformationList().add(Duration.ofMillis(20));
		accumulator.getInformationList().add(Duration.ofMillis(30));

		double p50 = accumulator.percentile(50.0); // median
		double p95 = accumulator.percentile(95.0);

		assertThat(p50, closeTo(0.02, 0.01)); // median ~20ms
		assertThat(p95, closeTo(0.03, 0.01)); // ~30ms
	}

	@Test
	void shouldReturnCorrectPercentilesForEvenNumberOfDurations() {
		accumulator.getInformationList().add(Duration.ofMillis(10));
		accumulator.getInformationList().add(Duration.ofMillis(20));
		accumulator.getInformationList().add(Duration.ofMillis(30));
		accumulator.getInformationList().add(Duration.ofMillis(40));

		double p50 = accumulator.percentile(50.0); // median
		double p90 = accumulator.percentile(90.0);

		assertThat(p50, equalTo(0.02)); // median is the 2nd value for even numbers
		assertThat(p90, equalTo(0.04)); // 90th percentile 40ms
	}

	@Test
	void shouldClampPercentileIndexAtEdges() {
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(10)));
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(20)));

		double p0 = accumulator.percentile(0.0);
		double p100 = accumulator.percentile(100.0);

		assertThat(p0, closeTo(0.01, 0.01)); // minimum value
		assertThat(p100, closeTo(0.02, 0.01)); // maximum value
	}

	@Test
	void shouldBuildCorrectStatistics() {
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(10)));
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(20)));
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(30)));
		accumulator.accumulate(() -> Threads.safeSleep(Duration.ofMillis(40)));

		DurationAccumulator.Statistics stats = accumulator.buildStatistics();

		assertThat(stats.getRequestCount(), equalTo(4));

		double expectedAvg = (0.01 + 0.02 + 0.03 + 0.04) / 4;
		assertThat(stats.getAvgRequestTime(), closeTo(expectedAvg, 0.01));
		assertThat(stats.getMaxRequestTime(), closeTo(0.04, 0.01));
		assertThat(stats.getP95RequestTime(), closeTo(0.04, 0.01));
		assertThat(stats.getP90RequestTime(), closeTo(0.04, 0.01));
	}

	@Test
	void shouldReturnFormattedDurationsOnStatisticsToString() {
		accumulator.getInformationList().add(Duration.ofMillis(10));
		accumulator.getInformationList().add(Duration.ofMillis(20));

		DurationAccumulator.Statistics stats = accumulator.buildStatistics();
		String output = stats.toString();

		assertThat(output, containsString("Count: 2"));
		assertThat(output, containsString("Avg time:"));
		assertThat(output, containsString("p95 time:"));
		assertThat(output, containsString("p90 time:"));
		assertThat(output, containsString("Max time:"));
	}

	@Test
	void shouldReturnFormattedDurationsOnAccumulatorToString() {
		accumulator.getInformationList().add(Duration.ofMillis(10));
		accumulator.getInformationList().add(Duration.ofMillis(20));

		accumulator.buildStatistics();
		String output = accumulator.toString();

		assertThat(output, containsString("Count: 2"));
		assertThat(output, containsString("Avg time:"));
		assertThat(output, containsString("p95 time:"));
		assertThat(output, containsString("p90 time:"));
		assertThat(output, containsString("Max time:"));
	}

	@Test
	void shouldReturnEmptyStringIfThereAreNoStatisticsBuiltOnAccumulatorToString() {
		String output = accumulator.toString();

		assertThat(output, is(emptyString()));
	}

	@Test
	void shouldThrowIllegalStateExceptionWhenBuildingStatisticsWithoutDurations() {
		Exception exception = null;

		try {
			accumulator.getStatistics();
		} catch (Exception e) {
			exception = e;
		}

		assertNotNull(exception);
		assertThat(exception.getClass(), equalTo(IllegalStateException.class));
		assertThat(exception.getMessage(), equalTo("No statistics available. Build statistics by calling buildStatistics() first."));
	}

}
