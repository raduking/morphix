package org.morphix.annotation;

/**
 * Used as internal annotation in the {@link Src} annotation.
 *
 * @author Radu Sebastian LAZIN
 */
public @interface From {

	Class<?> type();

	String path();

}
