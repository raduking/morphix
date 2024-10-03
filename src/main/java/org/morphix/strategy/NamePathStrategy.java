package org.morphix.strategy;

import org.morphix.reflection.ConverterField;

/**
 * Strategy that searches a field by transforming the destination field name to
 * a field path based on camel cases.
 *
 * @author Radu Sebastian LAZIN
 */
public class NamePathStrategy extends PathStrategy {

	@Override
	public ConverterField find(final Object source, final String sourceFieldName) {
		return super.find(source, convertToPath(sourceFieldName));
	}

	/**
	 * Converts a camel case string to a path string.
	 *
	 * @param sourceString source string
	 * @return transformed string
	 */
	protected static String convertToPath(final String sourceString) {
		String[] tokens = sourceString.split("(?=\\p{Lu})");
		return String.join(".", tokens).toLowerCase();
	}

}
