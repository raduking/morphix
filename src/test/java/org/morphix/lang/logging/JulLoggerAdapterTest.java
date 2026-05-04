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
package org.morphix.lang.logging;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.function.LoggerAdapter;

/**
 * Test class for {@link JulLoggerAdapter}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class JulLoggerAdapterTest {

	private static final String TEST = "test";

	@Mock
	private Logger logger;

	private JulLoggerAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = JulLoggerAdapter.of(logger);
	}

	@Test
	void shouldNotLogWhenLevelIsNotLoggable() {
		doReturn(false).when(logger).isLoggable(Level.INFO);

		adapter.log(LoggerAdapter.LoggingLevel.INFO, TEST);

		verify(logger, never()).log(any(), anyString());
		verify(logger, never()).log(any(), anyString(), any(Throwable.class));
	}

	@Test
	void shouldLogSimpleMessageWhenNoArgs() {
		doReturn(true).when(logger).isLoggable(Level.INFO);

		adapter.log(LoggerAdapter.LoggingLevel.INFO, TEST);

		verify(logger).log(Level.INFO, TEST);
	}

	@Test
	void shouldFormatMessageWhenArgsWithoutThrowable() {
		doReturn(true).when(logger).isLoggable(Level.INFO);

		adapter.log(LoggerAdapter.LoggingLevel.INFO, "Hello {}", "world");

		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

		verify(logger).log(eq(Level.INFO), messageCaptor.capture());
		assertThat(messageCaptor.getValue(), containsString("world"));
	}

	@Test
	void shouldLogExceptionWhenOnlyThrowableArg() {
		doReturn(true).when(logger).isLoggable(Level.SEVERE);
		RuntimeException ex = new RuntimeException("boom");

		adapter.log(LoggerAdapter.LoggingLevel.ERROR, TEST, ex);

		verify(logger).log(Level.SEVERE, TEST, ex);
	}

	@Test
	void shouldFormatMessageAndLogExceptionWhenMixedArgs() {
		doReturn(true).when(logger).isLoggable(Level.SEVERE);
		RuntimeException ex = new RuntimeException("boom");

		adapter.log(LoggerAdapter.LoggingLevel.ERROR, "Hello {}", "world", ex);

		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

		verify(logger).log(eq(Level.SEVERE), messageCaptor.capture(), eq(ex));
		assertThat(messageCaptor.getValue(), containsString("world"));
	}

	@Test
	void shouldBuildLoggerNameFromClass() {
		JulLoggerAdapter classAdapter = JulLoggerAdapter.of(JulLoggerAdapterTest.class);

		assertThat(classAdapter.getLogger().getName(), containsString("JulLoggerAdapterTest"));
	}

	@Test
	void shouldBuildLoggerNameFromString() {
		JulLoggerAdapter stringAdapter = JulLoggerAdapter.of("com.example.MyClass");

		assertThat(stringAdapter.getLogger().getName(), containsString("com.example.MyClass"));
	}
}
