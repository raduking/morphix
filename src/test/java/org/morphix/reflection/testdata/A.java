/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.reflection.testdata;

/**
 * Class used in testing.
 *
 * @author Radu Sebastian LAZIN
 */
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
