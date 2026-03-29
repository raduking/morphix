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
package org.morphix.lang.resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Lifecycle}.
 *
 * @author Radu Sebastian LAZIN
 */
class LifecycleTest {

	@Test
	void shouldBuildFromBoolean() {
		Lifecycle managed = Lifecycle.from(true);
		Lifecycle unmanaged = Lifecycle.from(false);

		assertThat(managed, equalTo(Lifecycle.MANAGED));
		assertThat(unmanaged, equalTo(Lifecycle.UNMANAGED));
	}

	@Test
	void shouldReturnManaged() {
		assertThat(Lifecycle.MANAGED.isManaged(), equalTo(true));
		assertThat(Lifecycle.UNMANAGED.isManaged(), equalTo(false));
	}

	@Test
	void shouldReturnUnmanaged() {
		assertThat(Lifecycle.MANAGED.isUnmanaged(), equalTo(false));
		assertThat(Lifecycle.UNMANAGED.isUnmanaged(), equalTo(true));
	}
}
