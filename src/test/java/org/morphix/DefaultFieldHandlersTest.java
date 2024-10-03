package org.morphix;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.ConstructorAccessor;

/**
 * Test class for {@link DefaultFieldHandlers}.
 *
 * @author Radu Sebastian LAZIN
 */
class DefaultFieldHandlersTest {

	static final FieldHandler[] FIELD_HANDLERS = {
			DefaultFieldHandlers.FIELD_HANDLER_NULL_SOURCE_SKIPPER,
			DefaultFieldHandlers.FIELD_HANDLER_STATIC_FIELD_SKIPPER,
			DefaultFieldHandlers.FIELD_HANDLER_DIRECT_ASSIGNMENT,
			DefaultFieldHandlers.FIELD_HANDLER_PRIMITIVE_ASSIGNMENT,
			DefaultFieldHandlers.FIELD_HANDLER_NUMBER_TO_NUMBER,
			DefaultFieldHandlers.FIELD_HANDLER_CHAR_SEQUENCE_TO_ENUM,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_STRING,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_CHAR_ARRAY,
			DefaultFieldHandlers.FIELD_HANDLER_CHAR_SEQUENCE_TO_ANY,
			DefaultFieldHandlers.FIELD_HANDLER_ITERABLE_TO_ITERABLE,
			DefaultFieldHandlers.FIELD_HANDLER_ARRAY_TO_ARRAY,
			DefaultFieldHandlers.FIELD_HANDLER_ITERABLE_TO_ARRAY,
			DefaultFieldHandlers.FIELD_HANDLER_ARRAY_TO_ITERABLE,
			DefaultFieldHandlers.FIELD_HANDLER_MAP_TO_MAP,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_ANY_FROM_STATIC_METHOD,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_ANY_FROM_CONSTRUCTOR,
			DefaultFieldHandlers.FIELD_HANDLER_ANY_TO_ITERABLE
	};

	@Test
	void shouldKeepTheHandlersOrder() {
		for (int i = 0; i < DefaultFieldHandlers.FIELD_HANDLERS_CHAIN.length; ++i) {
			assertThat(FIELD_HANDLERS[i], equalTo(DefaultFieldHandlers.FIELD_HANDLERS_CHAIN[i]));
		}
	}

	@Test
	void shouldHavePrivateConstructor() throws Exception {
		Constructor<DefaultFieldHandlers> constructor = DefaultFieldHandlers.class.getDeclaredConstructor();
		assertThat(Modifier.isPrivate(constructor.getModifiers()), equalTo(true));

		try (ConstructorAccessor<DefaultFieldHandlers> ignored = new ConstructorAccessor<>(constructor)) {
			constructor.newInstance();
		}
	}

	@Test
	void shouldReturnTheDefaultFieldHandlersOnList() {
		List<FieldHandler> result = DefaultFieldHandlers.list();

		assertThat(result, hasSize(FIELD_HANDLERS.length));
		assertThat(result, equalTo(DefaultFieldHandlers.FIELD_HANDLERS_LIST));
	}

}
