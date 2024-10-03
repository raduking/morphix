package org.morphix;

/**
 * Result from the field handler.
 *
 * @author Radu Sebastian LAZIN
 */
public enum FieldHandlerResult {

	/**
	 * Tells the converter that the handler converted the value successfully.
	 */
	CONVERTED(true),

	/**
	 * Tells the converter to move to the next handler in the chain.
	 */
	SKIP(false),

	/**
	 * Tells the converter to break the chain and consider the value handled but not converted.
	 */
	BREAK(true);

	/**
	 * handled flag
	 */
	private final boolean handled;

	FieldHandlerResult(final boolean handled) {
		this.handled = handled;
	}

	public boolean isHandled() {
		return handled;
	}

}
