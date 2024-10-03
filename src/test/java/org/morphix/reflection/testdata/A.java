package org.morphix.reflection.testdata;

public class A {

	public static final String FIELD_NAME = "field";

	private String field;
	private String s;

	int i;

	public Boolean b;

	public String getField() {
		return field;
	}

	public String getS() {
		return s;
	}

	public void setI(final int i) {
		this.i = i;
	}

	public void foo(final String s) {
		this.s = s;
	}

	@SuppressWarnings("unused")
	private void fooPrivate(final String s) {
		this.s = s;
	}

}
