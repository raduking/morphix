package org.morphix.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to specify which fields can be hidden or retrieved, based on
 * the defined scope.
 *
 * @author Radu Sebastian LAZIN
 */
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
public @interface Expandable {

	// empty

}
