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
package org.morphix.benchmark;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Compares the synchronized {@link Stack} baseline with the unsynchronized {@link ArrayList}-based stack used by
 * {@code StackedContextHolder} for its stack operations.
 *
 * <pre>
 * mvn jmh:benchmark -Pbenchmark -Djmh.benchmarks=org.morphix.benchmark.StackedContextHolderBenchmark
 * </pre>
 *
 * Benchmark results on JDK 21 (average nanoseconds per operation):
 *
 * <table>
 * <caption>{@link Stack} baseline versus the {@link ArrayList}-based stack</caption>
 * <tr>
 * <th>Operation</th>
 * <th>{@link Stack}</th>
 * <th>{@link ArrayList}-based</th>
 * </tr>
 * <tr>
 * <td>One scope</td>
 * <td>17.2</td>
 * <td>5.8</td>
 * </tr>
 * <tr>
 * <td>Five nested scopes</td>
 * <td>72.4</td>
 * <td>16.8</td>
 * </tr>
 * <tr>
 * <td>Twenty nested scopes</td>
 * <td>274.7</td>
 * <td>65.9</td>
 * </tr>
 * <tr>
 * <td>{@code changeElement}</td>
 * <td>17.3</td>
 * <td>4.9</td>
 * </tr>
 * </table>
 *
 * The unsynchronized implementation is approximately three to four times faster in this microbenchmark, but the
 * absolute difference remains small for the usual stack depths.
 *
 * @author Radu Sebastian LAZIN
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@State(Scope.Thread)
public class StackedContextHolderBenchmark {

	private static final String ELEMENT = "tenant";
	private static final String CHANGED_ELEMENT = "changed";

	@Param({ "1", "5", "20" })
	private int depth;

	private Stack<String> stack;
	private ArrayListStack<String> arrayListStack;

	@Setup
	public void setup() {
		stack = new Stack<>();
		arrayListStack = new ArrayListStack<>();
	}

	/**
	 * Measures the push/peek/pop sequence used for one context scope.
	 *
	 * @return the innermost element
	 */
	@Benchmark
	public String stackScope() {
		return scope(stack);
	}

	/**
	 * Measures the same scope sequence without per-method synchronization.
	 *
	 * @return the innermost element
	 */
	@Benchmark
	public String arrayListStackScope() {
		return scope(arrayListStack);
	}

	/**
	 * Measures the indexed replacement used by {@code changeElement}.
	 *
	 * @return the replaced element
	 */
	@Benchmark
	public String stackChangeElement() {
		stack.push(ELEMENT);
		String previous = stack.set(stack.size() - 1, CHANGED_ELEMENT);
		stack.pop();
		return previous;
	}

	/**
	 * Measures indexed replacement without per-method synchronization.
	 *
	 * @return the replaced element
	 */
	@Benchmark
	public String arrayListStackChangeElement() {
		arrayListStack.push(ELEMENT);
		String previous = arrayListStack.set(arrayListStack.size() - 1, CHANGED_ELEMENT);
		arrayListStack.pop();
		return previous;
	}

	private String scope(final Stack<String> currentStack) {
		for (int i = 0; i < depth; ++i) {
			currentStack.push(ELEMENT);
		}
		String currentElement = currentStack.peek();
		for (int i = 0; i < depth; ++i) {
			currentStack.pop();
		}
		return currentElement;
	}

	private String scope(final ArrayListStack<String> currentStack) {
		for (int i = 0; i < depth; ++i) {
			currentStack.push(ELEMENT);
		}
		String currentElement = currentStack.peek();
		for (int i = 0; i < depth; ++i) {
			currentStack.pop();
		}
		return currentElement;
	}

	private static class ArrayListStack<T> extends ArrayList<T> {

		private static final long serialVersionUID = 1L;

		T push(final T element) {
			add(element);
			return element;
		}

		T pop() {
			return remove(size() - 1);
		}

		T peek() {
			return get(size() - 1);
		}
	}
}
