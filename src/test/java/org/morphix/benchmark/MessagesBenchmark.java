/*
 * Copyright 2025 the original author or authors.
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
package org.morphix.benchmark;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.morphix.lang.Messages;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmark for different message formatting approaches.
 *
 * <ul>
 * <li>String concatenation using the {@code +} operator</li>
 * <li>Using a custom {@link Messages} utility for interpolation</li>
 * <li>Using {@link String#format}</li>
 * </ul>
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.morphix.benchmark.MessagesBenchmark
 * </pre>
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
@State(Scope.Thread)
public class MessagesBenchmark {

	private String methodName;
	private String type1;
	private String type2;

	@Setup
	public void setup() {
		methodName = "setValue";
		type1 = "java.lang.String";
		type2 = "int";
	}

	@Benchmark
	public String plusConcatenation() {
		return "Error finding method: "
				+ methodName + "(" + type1 + ") or "
				+ methodName + "(" + type2 + ")";
	}

	@Benchmark
	public String messagesInterpolation() {
		return message(
				"Error finding method: {}({}) or {}({})",
				methodName,
				type1,
				methodName,
				type2);
	}

	@Benchmark
	public String messagesInterpolationFast() {
		return Messages.message(
				"Error finding method: {}({}) or {}({})",
				methodName,
				type1,
				methodName,
				type2);
	}

	@Benchmark
	public String stringFormat() {
		return String.format(
				"Error finding method: %s(%s) or %s(%s)",
				methodName,
				type1,
				methodName,
				type2);
	}

	static String message(final String template, final Object... args) {
		if (null == template || null == args || args.length == 0) {
			return template;
		}
		int argumentLengthHeuristic = 16;
		final StringBuilder sb = new StringBuilder(template.length() + args.length * argumentLengthHeuristic);

		int argumentIndex = 0;
		int index = 0;
		while (index < template.length()) {
			if (index + 1 < template.length()
					&& template.charAt(index) == '{'
					&& template.charAt(index + 1) == '}'
					&& argumentIndex < args.length) {
				sb.append(Objects.toString(args[argumentIndex++]));
				index += 2;
			} else {
				sb.append(template.charAt(index++));
			}
		}
		return sb.toString();
	}
}
