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
package org.morphix.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.morphix.lang.function.Runnables;
import org.morphix.lang.function.SetterFunction;
import org.morphix.lang.function.Suppliers;
import org.morphix.reflection.Constructors;

/**
 * Utility methods that handle operations with null-able objects.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Nullables {

	/**
	 * Applies the function on the given value if the value is non-null. Otherwise, returns default value supplied by the
	 * default value supplier.
	 *
	 * @param <T> value type / function parameter type
	 * @param <U> return type
	 *
	 * @param value function parameter
	 * @param function function
	 * @param defaultValueSupplier default value supplier
	 * @return result of the function for non-null values otherwise defaultValue
	 */
	public static <T, U> U apply(final T value, final Function<T, U> function, final Supplier<U> defaultValueSupplier) {
		return null != value ? function.apply(value) : defaultValueSupplier.get();
	}

	/**
	 * Calls the function with the given parameter only if the parameter is not null.
	 *
	 * @param <T> parameter type
	 * @param <R> return type
	 *
	 * @param obj object to test for null
	 * @param function function to call otherwise null
	 * @return function result when parameter is not null, null otherwise
	 */
	public static <T, R> R apply(final T obj, final Function<T, R> function) {
		return apply(obj, function, supplyNull());
	}

	/**
	 * Returns the given value if the value is non-null. Otherwise, returns the value supplied by default value supplier.
	 * Prefer using this version since the default value will only be evaluated if the value is null.
	 *
	 * @param <T> value type
	 *
	 * @param value function parameter
	 * @param defaultValueSupplier supplies the default value when the given value is <code>null</code>
	 * @return the given value if the value is non-null. Otherwise, returns defaultValue
	 */
	public static <T> T nonNullOrDefault(final T value, final Supplier<T> defaultValueSupplier) {
		return null != value ? value : defaultValueSupplier.get();
	}

	/**
	 * Returns the given value if the value is non-null. Otherwise, returns defaultValue.
	 *
	 * @param <T> value type
	 *
	 * @param value function parameter
	 * @param defaultValue default value to return when the value is <code>null</code>
	 * @return the given value if the value is non-null. Otherwise, returns defaultValue
	 */
	public static <T> T nonNullOrDefault(final T value, final T defaultValue) {
		return nonNullOrDefault(value, () -> defaultValue);
	}

	/**
	 * Returns the given value if the value is non-null. Otherwise, throws the exception supplied by the exception supplier.
	 *
	 * @param <T> value type
	 * @param <E> exception type
	 *
	 * @param value function parameter
	 * @param throwableSupplier supplies the exception to be thrown when the value is <code>null</code>
	 * @return the given value if the value is non-null
	 */
	public static <T, E extends Throwable> T nonNullOrThrow(final T value, final Supplier<E> throwableSupplier) {
		// JaCoco false positive on missed branch: the method reThrow always throws an exception and functionally is covered
		return null != value ? value : Unchecked.Undeclared.reThrow(throwableSupplier.get());
	}

	/**
	 * Calls {@link Objects#requireNonNull(Object)} on all objects given as parameter.
	 *
	 * @param objects references to test for null
	 */
	public static void requireNotNull(final Object... objects) {
		for (Object object : objects) {
			Objects.requireNonNull(object);
		}
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the given object is not {@code null}.
	 *
	 * @param obj reference to test for null
	 * @param errorMessage the message for the exception thrown
	 */
	public static void requireNull(final Object obj, final String errorMessage) {
		if (null != obj) {
			throw new IllegalArgumentException(errorMessage);
		}
	}

	/**
	 * Calls the supplier method only if the parameter is not null.
	 *
	 * @param <T> supplied type
	 * @param <U> checked object type
	 *
	 * @param obj object to test for null
	 * @param statements supplier
	 * @return the supplier call result if the argument is not null
	 */
	public static <T, U> T whenNotNull(final U obj, final Supplier<T> statements) {
		return null != obj ? statements.get() : null;
	}

	/**
	 * Calls the runnable method only if the parameter is not null.
	 *
	 * @param <T> parameter type
	 *
	 * @param obj object to test for null
	 * @param statements runnable to execute when obj is not null
	 */
	public static <T> void whenNotNull(final T obj, final Runnable statements) {
		if (null != obj) {
			statements.run();
		}
	}

	/**
	 * Calls the consumer method only if the parameter is not null.
	 *
	 * @param <T> parameter type
	 *
	 * @param obj object to test for null
	 * @param statements consumer that accepts the value when obj is not null
	 */
	public static <T> void whenNotNull(final T obj, final Consumer<T> statements) {
		if (null != obj) {
			statements.accept(obj);
		}
	}

	/**
	 * Calls the setter function only if the value is not null.
	 *
	 * @param <T> parameter type
	 *
	 * @param value value to set
	 * @param setterFunction setter function
	 */
	public static <T> void set(final T value, final SetterFunction<T> setterFunction) {
		SetterFunction.nonNullSetter(setterFunction).set(value);
	}

	/**
	 * Calls the setter function with the given value only if the value is not null. If the given value is null the setter
	 * is called with the defaultValue. Prefer to use {@link #set(Object, Consumer, Supplier)} for lazy default value
	 * evaluation when the default value is not a constant.
	 *
	 * @param <T> parameter type
	 *
	 * @param value value to set
	 * @param setterFunction setter function
	 * @param defaultValue default value to set when value is null
	 */
	public static <T> void set(final T value, final Consumer<T> setterFunction, final T defaultValue) {
		set(value, setterFunction, (Supplier<T>) () -> defaultValue);
	}

	/**
	 * Calls the setter function with the given value only if the value is not null. If the given value is null the setter
	 * is called with the default value returned from the default value supplier.
	 *
	 * @param <T> parameter type
	 *
	 * @param value value to set
	 * @param setterFunction setter function
	 * @param defaultValueSupplier default value supplier when value is null
	 */
	public static <T> void set(final T value, final Consumer<T> setterFunction, final Supplier<T> defaultValueSupplier) {
		if (null != value) {
			setterFunction.accept(value);
		} else {
			setterFunction.accept(defaultValueSupplier.get());
		}
	}

	/**
	 * Returns a non-null unmodifiable list from an array. If the array is empty or null the resulting list is empty.
	 *
	 * @param <T> element type
	 *
	 * @param ts array
	 * @return non null list
	 */
	@SafeVarargs
	public static <T> List<T> nonNullList(final T... ts) {
		// using Arrays.asList(ts) instead of List.of(ts) because List.of(ts) with a varargs array can
		// throw NullPointerException if the array contains null elements.
		return null == ts ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(ts));
	}

	/**
	 * Returns a non-null unmodifiable list from a collection. If the collection is empty or null the resulting list is
	 * empty.
	 *
	 * @param <T> element type
	 *
	 * @param ts collection
	 * @return non null list
	 */
	public static <T> List<T> nonNullList(final Collection<T> ts) {
		return null == ts ? Collections.emptyList() : List.copyOf(ts);
	}

	/**
	 * Returns a non-null list from another list
	 *
	 * @param <T> element type
	 *
	 * @param ts list
	 * @return non null list
	 */
	public static <T> List<T> nonNullList(final List<T> ts) {
		return null == ts ? Collections.emptyList() : ts;
	}

	/**
	 * Helper method to write fluent style null conditions.
	 * <p>
	 * Examples:
	 *
	 * <pre>
	 * whenNotNull(obj.getSomething()).then(something -> do(something));
	 *
	 * whenNotNull(variable).then(() -> System.out.print("Something"));
	 *
	 * var suppliedWhenNotNull = whenNotNull(something).thenReturn(() -> "Anything");
	 * </pre>
	 *
	 * @param <T> value type
	 *
	 * @param value value to check
	 * @return null dispatcher object
	 */
	public static <T> Chain<T> whenNotNull(final T value) {
		return new Chain<>(value);
	}

	/**
	 * Helper method to write fluent style null conditions. This method has an error message parameter for when a fast fail
	 * is needed in case the value is null.
	 * <p>
	 * Examples:
	 *
	 * <pre>
	 * whenNotNull(obj.getSomething(), "value is null").then(something -> do(something));
	 * </pre>
	 *
	 * the method will fail fast with an {@link IllegalStateException} without making any other checks or method calls.
	 *
	 * @param <T> value type
	 *
	 * @param value value to check
	 * @param failFastErrorMessage error message
	 * @return null dispatcher object
	 */
	public static <T> Chain<T> whenNotNull(final T value, final String failFastErrorMessage) {
		return new Chain<>(value, failFastErrorMessage);
	}

	/**
	 * Calls the supplied value if the condition is true, otherwise {@code null}.
	 *
	 * @param <T> parameter type
	 * @param condition condition to test
	 * @param valueSupplier value supplier
	 * @return the supplier call result if it conforms to the condition, otherwise null
	 */
	public static <T> T whenOrElseNull(final boolean condition, final Supplier<T> valueSupplier) {
		return condition ? valueSupplier.get() : null;
	}

	/**
	 * Alias for {@link #whenNotNull(Object)}. This method should be used when pairing with <code>orElse</code> methods.
	 *
	 * <pre>
	 * String a = notNull(someString).orElse(() -> getSomeOtherString());
	 * String b = notNull(someString).orElse(SOME_STRING_CONSTANT);
	 * </pre>
	 *
	 * @param <T> value type
	 *
	 * @param value value to check
	 * @return null dispatcher object
	 */
	public static <T> Chain<T> notNull(final T value) {
		return whenNotNull(value);
	}

	/**
	 * Alias for {@link #whenNotNull(Object, String)}.
	 *
	 * @param <T> value type
	 *
	 * @param value value to check
	 * @param failFastErrorMessage error message
	 * @return null dispatcher object
	 */
	public static <T> Chain<T> notNull(final T value, final String failFastErrorMessage) {
		return whenNotNull(value, failFastErrorMessage);
	}

	/**
	 * Returns a supplier which supplies <code>null</code>.
	 *
	 * @param <T> generic type
	 *
	 * @return a supplier which supplies a null value
	 */
	public static <T> Supplier<T> supplyNull() {
		return Suppliers.supplyNull();
	}

	/**
	 * Returns a supplier which runs the runnable and supplies <code>null</code>.
	 *
	 * @param <T> generic type
	 *
	 * @param runnable code to run before supplying <code>null</code>
	 * @return a supplier which runs the runnable and supplies a null value
	 */
	public static <T> Supplier<T> supplyNull(final Runnable runnable) {
		return Runnables.compose(runnable, supplyNull());
	}

	/**
	 * Private constructor.
	 */
	private Nullables() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Helper class for fluent style null checks.
	 *
	 * @param <T> value type
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Chain<T> {

		/**
		 * Value to test for <code>null</code>
		 */
		private final T value;

		/**
		 * Constructor with value.
		 *
		 * @param value value to test for <code>null</code>
		 */
		public Chain(final T value) {
			this(value, null);
		}

		/**
		 * Constructor with value and fail fast error message.
		 *
		 * @param value value to test for <code>null</code>
		 * @param failFastErrorMessage exception message if value is <code>null</code>
		 */
		public Chain(final T value, final String failFastErrorMessage) {
			if (null == value && null != failFastErrorMessage) {
				throw new IllegalStateException(failFastErrorMessage);
			}
			this.value = value;
		}

		/**
		 * Chain a consumer for the value.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param consumer consumer that accepts the value
		 */
		public void then(final Consumer<T> consumer) {
			if (null != value) {
				consumer.accept(value);
			}
		}

		/**
		 * Chain a runnable if the value is not <code>null</code>.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param runnable code to run if value is not <code>null</code>
		 */
		public void then(final Runnable runnable) {
			if (null != value) {
				runnable.run();
			}
		}

		/**
		 * Returns the supplied value by the given supplier if value is not <code>null</code>.
		 * <p>
		 * This is an intermediate operation.
		 * </p>
		 *
		 * @param <U> return type
		 *
		 * @param supplier return value supplier
		 * @return the supplied value by the given supplier if value is not <code>null</code>
		 */
		public <U> Chain<U> thenYield(final Supplier<U> supplier) {
			return thenYield((final T t) -> supplier.get());
		}

		/**
		 * Returns the value returned by the function applied to the existing value if value is not <code>null</code>.
		 * <p>
		 * This is an intermediate operation.
		 * </p>
		 *
		 * @param <U> return type
		 *
		 * @param function return value function
		 * @return the value returned by the function
		 */
		public <U> Chain<U> thenYield(final Function<T, U> function) {
			return andNotNull(function);
		}

		/**
		 * Returns the value returned by the function applied to the existing value if value is not <code>null</code>.
		 * <p>
		 * This is an intermediate operation.
		 * </p>
		 *
		 * @param <U> return type
		 *
		 * @param function return value function
		 * @return the value returned by the function
		 */
		public <U> Chain<U> thenNotNull(final Function<T, U> function) {
			return andNotNull(function);
		}

		/**
		 * Returns the value returned by the function applied to the existing value if value is not <code>null</code>.
		 * <p>
		 * This is an intermediate operation.
		 * </p>
		 *
		 * @param <U> return type
		 *
		 * @param function return value function
		 * @return the value returned by the function
		 */
		public <U> Chain<U> andNotNull(final Function<T, U> function) {
			return new Chain<>(thenReturn(function));
		}

		/**
		 * Returns the value returned by the function applied to the existing value if value is not <code>null</code>.
		 * <p>
		 * This is an intermediate operation.
		 * </p>
		 *
		 * @param <U> return type
		 *
		 * @param function return value function
		 * @param failFastErrorMessage exception error message on fast fail
		 * @return the value returned by the function
		 */
		public <U> Chain<U> andNotNull(final Function<T, U> function, final String failFastErrorMessage) {
			return new Chain<>(thenReturn(function), failFastErrorMessage);
		}

		/**
		 * Returns the supplied value by the given supplier if value is not <code>null</code>.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param <U> return type
		 *
		 * @param supplier return value supplier
		 * @return the supplied value by the given supplier if value is not <code>null</code>
		 */
		public <U> U thenReturn(final Supplier<U> supplier) {
			return null != value ? supplier.get() : null;
		}

		/**
		 * Returns the value returned by the function applied to the existing value if value is not <code>null</code>.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param <U> return type
		 *
		 * @param function return value function
		 * @return the value returned by the function
		 */
		public <U> U thenReturn(final Function<T, U> function) {
			return null != value ? function.apply(value) : null;
		}

		/**
		 * Returns the value returned by the function applied to the existing value if value is not <code>null</code> otherwise
		 * returns the supplied value from the given supplier.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param <U> return type
		 *
		 * @param function return value function
		 * @param supplier default value supplier
		 * @return the value returned by the function
		 */
		public <U> U thenOrDefault(final Function<T, U> function, final Supplier<U> supplier) {
			return null != value ? function.apply(value) : supplier.get();
		}

		/**
		 * Returns the value if value is not <code>null</code>.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @return the value if value is not <code>null</code>
		 */
		public T thenReturn() {
			return thenReturn(Function.identity());
		}

		/**
		 * Returns the supplied value by the given supplier if value is <code>null</code>.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param defaultValueSupplier default value supplier
		 * @return the supplied value by the given supplier if value is <code>null</code>
		 */
		public T orElse(final Supplier<T> defaultValueSupplier) {
			return valueOrDefault(defaultValueSupplier);
		}

		/**
		 * Returns given default value if value is <code>null</code>.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param defaultValue default value
		 * @return given default value if value is <code>null</code>
		 */
		public T orElse(final T defaultValue) {
			return valueOrDefault(defaultValue);
		}

		/**
		 * Returns the supplied value by the given supplier if value is <code>null</code>.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param defaultValueSupplier default value supplier
		 * @return the supplied value by the given supplier if value is <code>null</code>
		 */
		public T valueOrDefault(final Supplier<T> defaultValueSupplier) {
			return null != value ? value : defaultValueSupplier.get();
		}

		/**
		 * Returns the given default value if value is <code>null</code>.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param defaultValue default value
		 * @return given default value if value is <code>null</code>
		 */
		public T valueOrDefault(final T defaultValue) {
			return null != value ? value : defaultValue;
		}

		/**
		 * Throws an {@link IllegalStateException} if value is <code>null</code> with the given fail fast error message as the
		 * exception message.
		 * <p>
		 * This is a terminal operation.
		 * </p>
		 *
		 * @param failFastErrorMessage exception message
		 * @return value if value is not <code>null</code>
		 * @throws IllegalStateException when value is null
		 */
		public T valueOrError(final String failFastErrorMessage) {
			if (null != value) {
				return value;
			}
			throw new IllegalStateException(failFastErrorMessage);
		}

		/**
		 * Returns the value if it's not null and conforms to the given predicate. Otherwise, the default value is returned.
		 *
		 * @param predicate value predicate
		 * @param defaultValue default value.
		 * @return value if value is not <code>null</code> and matches predicate
		 */
		public T valueWhenOrDefault(final Predicate<T> predicate, final T defaultValue) {
			return valueWhenOrDefault(predicate, () -> defaultValue);
		}

		/**
		 * Returns the value if it's not null and conforms to the given predicate. Otherwise, the default value supplied by the
		 * given supplier is returned.
		 *
		 * @param predicate value predicate
		 * @param defaultValueSupplier default value supplier
		 * @return value if value is not <code>null</code> and matches predicate
		 */
		public T valueWhenOrDefault(final Predicate<T> predicate, final Supplier<T> defaultValueSupplier) {
			return null != value && predicate.test(value) ? value : defaultValueSupplier.get();
		}

		/**
		 * Returns the value if it's not null and conforms to the given predicate. Otherwise, an {@link IllegalStateException}
		 * is thrown with the given error message.
		 *
		 *
		 * @param predicate value predicate
		 * @param failFastErrorMessage error message
		 * @return value if value is not <code>null</code> and matches predicate
		 * @throws IllegalStateException when value is null or does not match predicate
		 */
		public T valueWhenOrError(final Predicate<T> predicate, final String failFastErrorMessage) {
			if (null != value && predicate.test(value)) {
				return value;
			}
			throw new IllegalStateException(failFastErrorMessage);
		}

	}

}
