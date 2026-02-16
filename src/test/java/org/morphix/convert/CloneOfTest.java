/*
 * Copyright 2026 the original author or authors.
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
package org.morphix.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link Conversions#cloneOf(Object)} class.
 *
 * @author Radu Sebastian LAZIN
 */
class CloneOfTest {

	private static final int TEST_INT = 666;

	public static class A {
		int i;
	}

	@Test
	void shouldCloneAnObject() {
		A a = new A();
		a.i = TEST_INT;

		A cloneA = Conversions.cloneOf(a);

		assertThat(cloneA.i, equalTo(a.i));
		assertNotEquals(cloneA, a);
	}

	@Test
	void shouldReturnNullOnCloningNull() {
		Object result = Conversions.cloneOf(null);
		assertThat(result, equalTo(null));
	}

	public static class D {

		D temporaryD;
		D parentD;

		public void setTemporaryD(final D temporaryD) {
			this.temporaryD = temporaryD;
			if (temporaryD.getParentD() == null) {
				this.temporaryD.setParentD(this);
			}
		}

		public D getTemporaryD() {
			return temporaryD;
		}

		public void setParentD(final D parentD) {
			this.parentD = parentD;
		}

		public D getParentD() {
			return parentD;
		}
	}

	@Test
	void shouldCloneCorrectlyOnParentChildRelationships() {
		D d = new D();
		D tempD = new D();
		d.setTemporaryD(tempD);

		assertThat(d, equalTo(d.temporaryD.parentD));

		D dr = Conversions.cloneOf(d);

		assertNotNull(dr);
		assertThat(d, equalTo(d.temporaryD.parentD));
		assertThat(d.temporaryD, equalTo(tempD));
	}
}
