package org.morphix.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to annotate destination fields so that the converter knows
 * how to convert it from source.
 *
 * @author Radu Sebastian LAZIN
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Src {

	/**
	 * Source field name.
	 *
	 * @return field name
	 */
	String value() default "";

	/**
	 * Source field name.
	 *
	 * @return field name
	 */
	String name() default "";

	/**
	 * Defines class and name.
	 *
	 * @return from annotation array
	 */
	From[] from() default {};
}
