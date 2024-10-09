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
