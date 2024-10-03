package org.morphix.extra;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.morphix.annotation.Expandable;
import org.morphix.reflection.ConverterField;

/**
 * Keeps a list with the fields that need to be expanded.
 * <p>
 * When construction this object the <code>expandedFieldNames</code> list
 * parameter has the following conventions:
 * <ul>
 * <li><code>null</code> - all fields will be expanded ({@link Expandable}
 * annotation will be ignored)</li>
 * <li>empty list - no fields will be expanded (all fields with
 * {@link Expandable} annotation present will be empty)</li>
 * <li>non empty list - only the fields present in the list will be expanded all
 * others will be empty</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class ExpandableFields {

	/**
	 * {@link ExpandableFields} object with no expandable fields list which will
	 * result in all fields expanded.
	 */
	private static final ExpandableFields EXPAND_ALL = new ExpandableFields(null);

	/**
	 * {@link ExpandableFields} object with empty expandable fields list which
	 * will result in no fields expanded.
	 */
	private static final ExpandableFields EXPAND_NONE = new ExpandableFields(Collections.emptyList());

	/**
	 * List of fields to be expanded. Read the documentation about the
	 * conventions of this field in the {@link #of(List)} method.
	 */
	private final List<String> expandableFieldNames;

	/**
	 * Private constructor.
	 *
	 * @param expandableFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 */
	private ExpandableFields(final List<String> expandableFieldNames) {
		this.expandableFieldNames = expandableFieldNames;
	}

	/**
	 * Returns an {@link ExpandableFields} object.
	 *
	 * <p>
	 * The <code>expandedFieldNames</code> list parameter has the following
	 * conventions:
	 * <ul>
	 * <li><code>null</code> - all fields will be expanded ({@link Expandable}
	 * annotation will be ignored)</li>
	 * <li>empty list - no fields will be expanded (all fields with
	 * {@link Expandable} annotation present will be empty)</li>
	 * <li>non empty list - only the fields present in the list will be expanded
	 * all others will be empty</li>
	 * </ul>
	 *
	 * @param expandableFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 * @return an {@link ExpandableFields} object.
	 */
	public static ExpandableFields of(final List<String> expandableFieldNames) {
		if (null == expandableFieldNames) {
			return expandAll();
		}
		return new ExpandableFields(expandableFieldNames);
	}

	/**
	 * Variation of method {@link ExpandableFields#of(List)}.
	 *
	 * @param expandableFieldNames fields that will be expanded and have the
	 *            {@link Expandable} annotation.
	 * @return an {@link ExpandableFields} object.
	 */
	public static ExpandableFields of(final String... expandableFieldNames) {
		return null == expandableFieldNames ? of((List<String>) null) : of(List.of(expandableFieldNames));
	}

	/**
	 * Returns an object that will result in all expandable fields to be
	 * expanded.
	 *
	 * @return an object that will result in all expandable fields to be
	 *         expanded
	 */
	public static ExpandableFields expandAll() {
		return EXPAND_ALL;
	}

	/**
	 * Returns an object that will result in all expandable fields to be
	 * expanded.
	 *
	 * @return an object that will result in all expandable fields to be
	 *         expanded
	 */
	public static ExpandableFields expandNone() {
		return EXPAND_NONE;
	}

	/**
	 * Returns true if the field should not be expanded, false otherwise.
	 *
	 * @param fop field object pair
	 * @return true if the field should not be expanded, false otherwise.
	 */
	public boolean shouldNotExpandField(final ConverterField fop) {
		return isExpandableField(fop) && !shouldExpandFieldNoAnnotationCheck(fop);
	}

	/**
	 * Returns true if it should expand all fields, false otherwise.
	 *
	 * @return true if it should expand all fields, false otherwise.
	 */
	public boolean shouldExpandAllFields() {
		return null == expandableFieldNames;
	}

	/**
	 * Returns true if the field should be expanded, false otherwise. It doesn't
	 * check for {@link Expandable} annotation presence.
	 *
	 * @param field field to check
	 * @return true if the field should be expanded, false otherwise.
	 */
	private boolean shouldExpandFieldNoAnnotationCheck(final ConverterField field) {
		return shouldExpandAllFields() || expandableFieldNames.contains(field.getName());
	}

	/**
	 * Returns true if the field has the {@link Expandable} annotation present,
	 * false otherwise.
	 *
	 * @param field field to check
	 * @return true if the field has the {@link Expandable} annotation present,
	 *         false otherwise
	 */
	public static boolean isExpandableField(final ConverterField field) {
		return field.isAnnotationPresent(Expandable.class);
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
		ExpandableFields expandableFields = (ExpandableFields) obj;
		return Objects.equals(expandableFieldNames, expandableFields.expandableFieldNames);
	}

	/**
	 * see {@link Object#hashCode()}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(expandableFieldNames);
	}
}
