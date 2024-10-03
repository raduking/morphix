package org.morphix.extra;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.morphix.reflection.ConverterField;

/**
 * Keeps a list with the fields that need to be excluded.
 * <p>
 * When construction this object the <code>excludedFieldNames</code> list
 * parameter has the following conventions:
 * <ul>
 * <li><code>null</code> - no fields will be excluded</li>
 * <li>empty list - all fields will be excluded</li>
 * <li>non empty list - only the fields present in the list will be excluded all
 * others will be converted</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class ExcludedFields {

	/**
	 * {@link ExcludedFields} object with no excluded fields list which will
	 * result in no fields excluded.
	 */
	private static final ExcludedFields EXCLUDE_NONE = new ExcludedFields(null);

	/**
	 * {@link ExcludedFields} object with empty excluded fields list which will
	 * result in all fields excluded.
	 */
	private static final ExcludedFields EXCLUDE_ALL = new ExcludedFields(Collections.emptyList());

	/**
	 * List of fields to be expanded. Read the documentation about the
	 * conventions of this field in the {@link #of(List)} method.
	 */
	private final List<String> excludedFieldNames;

	/**
	 * Private constructor.
	 *
	 * @param excludedFieldNames fields that will be excluded
	 */
	private ExcludedFields(final List<String> excludedFieldNames) {
		this.excludedFieldNames = excludedFieldNames;
	}

	/**
	 * Returns an {@link ExpandableFields} object.
	 *
	 * <p>
	 * The <code>excludedFieldNames</code> list parameter has the following
	 * conventions:
	 * <ul>
	 * <li><code>null</code> - no fields will be excluded</li>
	 * <li>empty list - all fields will be excluded</li>
	 * <li>non empty list - only the fields present in the list will be excluded
	 * all others will be converted</li>
	 * </ul>
	 *
	 * @param excludedFieldNames fields that will be excluded
	 * @return an {@link ExcludedFields} object.
	 */
	public static ExcludedFields of(final List<String> excludedFieldNames) {
		if (null == excludedFieldNames) {
			return excludeNone();
		}
		if (excludedFieldNames.isEmpty()) {
			return excludeAll();
		}
		return new ExcludedFields(excludedFieldNames);
	}

	/**
	 * Variation of method {@link ExcludedFields#of(List)}.
	 *
	 * @param excludedFieldNames fields that will be excluded
	 * @return an {@link ExcludedFields} object.
	 */
	public static ExcludedFields of(final String... excludedFieldNames) {
		if (null == excludedFieldNames) {
			return excludeNone();
		}
		if (excludedFieldNames.length == 0) {
			return excludeAll();
		}
		return new ExcludedFields(List.of(excludedFieldNames));
	}

	/**
	 * Same as {@link ExcludedFields#of(String...)}.
	 *
	 * @param excludedFieldNames fields that will be excluded
	 * @return an {@link ExcludedFields} object.
	 */
	public static ExcludedFields exclude(final String... excludedFieldNames) {
		return of(excludedFieldNames);
	}

	/**
	 * Returns an object that will result in all fields to be excluded.
	 *
	 * @return an object that will result in all fields to be excluded
	 */
	public static ExcludedFields excludeAll() {
		return EXCLUDE_ALL;
	}

	/**
	 * Returns an object that will result in no excluded fields.
	 *
	 * @return an object that will result in no excluded fields
	 */
	public static ExcludedFields excludeNone() {
		return EXCLUDE_NONE;
	}

	/**
	 * Returns true if it should exclude all fields, false otherwise.
	 *
	 * @return true if it should exclude all fields, false otherwise.
	 */
	public boolean shouldExcludeAllFields() {
		return null != excludedFieldNames && excludedFieldNames.isEmpty();
	}

	/**
	 * Returns true if the field should be excluded, false otherwise.
	 *
	 * @param field field to check
	 * @return true if the field should be excluded, false otherwise
	 */
	public boolean shouldExcludeField(final ConverterField field) {
		return shouldExcludeAllFields() || (null != excludedFieldNames && excludedFieldNames.contains(field.getName()));
	}

	/**
	 * see {@link Object#equals(Object)}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExcludedFields expandableFields = (ExcludedFields) obj;
		return Objects.equals(excludedFieldNames, expandableFields.excludedFieldNames);
	}

	/**
	 * see {@link Object#hashCode()}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(excludedFieldNames);
	}

	/**
	 * see {@link Object#toString()}
	 */
	@Override
	public String toString() {
		if (null == excludedFieldNames) {
			return "No excluded fields";
		}
		if (excludedFieldNames.isEmpty()) {
			return "All fields are excluded";
		}
		return "Excluded fields: " + excludedFieldNames;
	}
}
