package org.morphix.lang.thread;

import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.morphix.lang.function.Runnables;
import org.morphix.lang.function.SetterFunction;

/**
 * Abstract thread context holder with a stack that provides the appropriate context even in recursive method calls.
 *
 * @param <T> stack element type
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class StackedContextHolder<T> {

	/**
	 * Logger for this class (JUL).
	 */
	private static final Logger LOGGER = Logger.getLogger(StackedContextHolder.class.getName());

	/**
	 * Stack element name for logging purposes.
	 */
	private final String elementName;

	/**
	 * Predicate to test wrong elements. This predicate is used to validate the element before adding it to the stack. If
	 * the predicate returns true, an exception is thrown and the element is not added to the stack.
	 */
	private final Predicate<T> wrongElementPredicate;

	/**
	 * Constructor.
	 *
	 * @param elementName stack element name
	 * @param wrongElementPredicate predicate to test wrong elements
	 */
	protected StackedContextHolder(final String elementName, final Predicate<T> wrongElementPredicate) {
		this.elementName = elementName;
		this.wrongElementPredicate = wrongElementPredicate;
	}

	/**
	 * Returns the element name.
	 *
	 * @return the element name
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * Returns the wrong element predicate.
	 *
	 * @return the wrong element predicate
	 */
	public Predicate<T> getWrongElementPredicate() {
		return wrongElementPredicate;
	}

	/**
	 * Returns a {@link ThreadLocal} containing a stack defined in the derived class.
	 *
	 * @return a thread local object containing a stack
	 */
	public abstract ThreadLocal<Stack<T>> getThreadStack();

	/**
	 * Clears the thread stack. Implement this method to do cleanup including call to {@link ThreadLocal#remove()} method.
	 */
	public void clearThreadStack() {
		getThreadStack().remove();
	}

	/**
	 * Returns the stack object from the thread local.
	 *
	 * @return the stack object from the thread local
	 */
	protected Stack<T> getStack() {
		return getThreadStack().get();
	}

	/**
	 * Runs the given statements on the given element as context and returns the result from the statements.
	 *
	 * @param <E> element type
	 * @param <U> return type
	 *
	 * @param element element
	 * @param statements statements to run
	 * @return statements result
	 */
	public static <E, U> U on(final E element, final SetterFunction<E> elementSetter, final Runnable elementRemover, final Supplier<U> statements) {
		try {
			elementSetter.set(element);
			return statements.get();
		} finally {
			elementRemover.run();
		}
	}

	/**
	 * Runs the given statements on the given element as context and returns the result from the statements.
	 *
	 * @param <E> element type
	 * @param <U> return type
	 * @param <V> stacked context holder type
	 *
	 * @param element element
	 * @param contextHolder stacked context holder instance
	 * @param statements statements to run
	 * @return statements result
	 */
	public static <E, U, V extends StackedContextHolder<E>> U on(final E element, final V contextHolder, final Supplier<U> statements) {
		return on(element, contextHolder::setElement, contextHolder::clearElement, statements);
	}

	/**
	 * Runs the given function on the given element as context and returns its result.
	 *
	 * @param <E> element type
	 * @param <U> function parameter type
	 * @param <R> function return type
	 *
	 * @param element element
	 * @param function function to run
	 * @param arg function argument
	 * @return function result
	 */
	public static <E, U, R> R on(final E element, final SetterFunction<E> elementSetter, final Runnable elementRemover, final Function<U, R> function,
			final U arg) {
		try {
			elementSetter.set(element);
			return function.apply(arg);
		} finally {
			elementRemover.run();
		}
	}

	/**
	 * Runs the given function on the given element as context and returns its result.
	 *
	 * @param <E> element type
	 * @param <U> function parameter type
	 * @param <R> function return type
	 * @param <V> stacked context holder type
	 *
	 * @param element element
	 * @param contextHolder stacked context holder instance
	 * @param function function to run
	 * @param arg function argument
	 * @return function result
	 */
	public static <E, U, R, V extends StackedContextHolder<E>> R on(final E element, final V contextHolder, final Function<U, R> function,
			final U arg) {
		return on(element, contextHolder::setElement, contextHolder::clearElement, function, arg);
	}

	/**
	 * Runs the given runnable on the given element.
	 *
	 * @param <E> element type
	 *
	 * @param element element
	 * @param runnable runnable to run
	 */
	public static <E> void on(final E element, final SetterFunction<E> elementSetter, final Runnable elementRemover, final Runnable runnable) {
		on(element, elementSetter, elementRemover, Runnables.toSupplier(runnable));
	}

	/**
	 * Runs the given runnable on the given element.
	 *
	 * @param <E> element type
	 * @param <V> stacked context holder type
	 *
	 * @param element element
	 * @param contextHolder stacked context holder
	 * @param runnable runnable to run
	 */
	public static <E, V extends StackedContextHolder<E>> void on(final E element, final V contextHolder, final Runnable runnable) {
		on(element, contextHolder::setElement, contextHolder::clearElement, runnable);
	}

	/**
	 * Adds the element to the current running stack.
	 *
	 * @param element element to add to the stack
	 */
	public void setElement(final T element) {
		debug(LOGGER, "[START:on{}]: {}", getElementName(), element);
		if (getWrongElementPredicate().test(element)) {
			throw new ThreadContextException(getElementName() + " cannot be: " + element);
		}
		Stack<T> currentStack = getStack();
		if (!isInitialized(currentStack)) {
			currentStack = new Stack<>();
			getThreadStack().set(currentStack);
		}
		currentStack.push(element);
	}

	/**
	 * Returns the current direction.
	 *
	 * @return the current direction
	 */
	public T getElement() {
		Stack<T> currentStack = getStack();
		return isEmpty(currentStack) ? null : currentStack.peek();
	}

	/**
	 * Changes the current element.
	 *
	 * @param element new element
	 */
	public void changeElement(final T element) {
		debug(LOGGER, "[change{}]: {}", getElementName(), element);
		if (null == element) {
			throw new ThreadContextException(getElementName() + " cannot be null.");
		}
		Stack<T> currentStack = getStack();
		if (!isEmpty(currentStack)) {
			currentStack.set(currentStack.size() - 1, element);
		}
	}

	/**
	 * Clears the current direction from the stack.
	 */
	protected void clearElement() {
		Stack<T> currentStack = getStack();
		T element = null;
		if (isEmpty(currentStack)) {
			clearThreadStack();
		} else {
			element = currentStack.pop();
			if (isEmpty(currentStack)) {
				clearThreadStack();
			}
		}
		debug(LOGGER, "[END:on{}]: {}", getElementName(), element);
	}

	/**
	 * Returns true if the current stack was initialized, false otherwise.
	 *
	 * @return true if the current stack was initialized, false otherwise
	 */
	protected static <T> boolean isInitialized(final Stack<T> currentStack) {
		return null != currentStack;
	}

	/**
	 * Returns true if the current stack is empty, false otherwise.
	 *
	 * @return true if the current stack is empty, false otherwise
	 */
	protected static <T> boolean isEmpty(final Stack<T> currentStack) {
		return null == currentStack || currentStack.isEmpty();
	}

	/**
	 * Logs the given message and arguments on debug level.
	 *
	 * @param logger logger to log with
	 * @param message message to log
	 * @param args message arguments
	 */
	protected static void debug(final Logger logger, final String message, final Object... args) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(String.format(message, args));
		}
	}
}
