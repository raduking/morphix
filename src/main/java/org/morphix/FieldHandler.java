package org.morphix;

import static org.morphix.reflection.predicates.MethodPredicates.isConverterMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.morphix.reflection.ConverterField;
import org.morphix.reflection.predicates.MethodPredicates;

/**
 * Base class for source field to destination field handling.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class FieldHandler {

	/**
	 * Configuration.
	 */
	private final Configuration configuration;

	/**
	 * Constructor with configuration.
	 *
	 * @param configuration configuration
	 */
	protected FieldHandler(final Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Default constructor.
	 */
	protected FieldHandler() {
		// initialize it with null to lazy load default configuration
		this(null);
	}

	/**
	 * Handles the field.
	 *
	 * @param sfo source field object pair
	 * @param dfo destination field object pair
	 * @return handler result which can be checked if the handling succeeded or not
	 */
	public abstract FieldHandlerResult handle(final ConverterField sfo, final ConverterField dfo);

	/**
	 * Returns true if the handler should try to handle the fields, false
	 * otherwise.
	 *
	 * @param sfo source field object pair
	 * @param dfo destination field object pair
	 * @return true if the field should be handled, false otherwise
	 */
	public boolean condition(final ConverterField sfo, final ConverterField dfo) {
		return true;
	}

	/**
	 * Returns the source type constraint.
	 *
	 * @return the source type constraint
	 */
	protected Predicate<Type> sourceTypeConstraint() {
		return type -> true;
	}

	/**
	 * Returns the destination type constraint.
	 *
	 * @return the destination type constraint
	 */
	protected Predicate<Type> destinationTypeConstraint() {
		return type -> true;
	}

	/**
	 * Returns the configuration.
	 *
	 * @return the configuration
	 */
	protected Configuration getConfiguration() {
		return null == configuration ? Configuration.defaultConfiguration() : configuration;
	}

	/**
	 * Returns a list of methods that verify with
	 * {@link MethodPredicates#isConverterMethod(Class)}.
	 *
	 * @param cls class in which to search the methods
	 * @return list of methods
	 */
	protected static List<Method> getConverterMethods(final Class<?> cls, final Class<?> srcClass) {
		return Arrays.stream(cls.getDeclaredMethods())
				.filter(isConverterMethod(srcClass))
				.toList();
	}

	/**
	 * Handle the data exchange between source and destination.
	 */
	protected boolean convert(final ConverterField sfo, final ConverterField dfo) {
		boolean typeConstraintsMet = sfo.typeMeets(sourceTypeConstraint()) && dfo.typeMeets(destinationTypeConstraint());
		if (typeConstraintsMet && condition(sfo, dfo)) {
			FieldHandlerResult result = handle(sfo, dfo);
			return result.isHandled();
		}
		return false;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		// we ignore the configuration
		return Objects.equals(obj.getClass(), getClass());
	}

	@Override
	public int hashCode() {
		return super.hashCode() + getClass().hashCode();
	}

}
