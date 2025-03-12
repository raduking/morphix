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
package org.morphix.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaObjects}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaObjectsTest {

	@Test
	void shouldCastToInferredType() {
		Object o = "Bubu";

		String bubu = JavaObjects.cast(o);

		assertThat(bubu, notNullValue());
		assertThat(bubu, equalTo("Bubu"));
	}

}
