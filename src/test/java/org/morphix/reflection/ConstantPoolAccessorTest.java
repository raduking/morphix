package org.morphix.reflection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Member;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ConstantPoolAccessor}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConstantPoolAccessorTest {

	private ConstantPoolAccessor victim = new ConstantPoolAccessor();

	@Test
	void shouldReturnFalseOnIsUsable() {
		assertThat(victim.isUsable(), equalTo(false));
	}

	@Test
	void shouldReturnNullOnGetConstantPool() {
		Object constantPool = victim.getConstantPool(String.class);
		assertThat(constantPool, equalTo(null));
	}

	@Test
	void shouldReturnZeroOnGetSize() {
		int size = victim.getSize(String.class);
		assertThat(size, equalTo(0));
	}

	@Test
	void shouldReturnNullOnGetMethodAt() {
		Member member = victim.getMethodAt(String.class, 1);
		assertThat(member, equalTo(null));
	}

	@Test
	void shouldReturnNonNullInstance() {
		ConstantPoolAccessor constantPoolAccessor = ConstantPoolAccessor.getInstance();
		assertThat(constantPoolAccessor, notNullValue());
	}
}
