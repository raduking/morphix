package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link Conversion#cloneOf(Object)} class.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterCloneOfTest {

	private static final int INT_13 = 13;

	public static class A {
		int i;
	}

	@Test
	void shouldCloneAnObject() {
		A a = new A();
		a.i = INT_13;

		A cloneA = Conversion.cloneOf(a);

		assertThat(cloneA.i, equalTo(a.i));
		assertNotEquals(cloneA, a);
	}

	@Test
	void shouldReturnNullOnCloningNull() {
		Object result = Conversion.cloneOf(null);
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

		D dr = Conversion.cloneOf(d);

		assertNotNull(dr);
		assertThat(d, equalTo(d.temporaryD.parentD));
		assertThat(d.temporaryD, equalTo(tempD));
	}
}
