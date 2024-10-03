package org.morphix.reflection.jvm;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

class MemberAccessorOracleJDKTest {

	private static final Set<String> EXCLUDED_FIELDS = Set.of("FIELD_NAME_THE_UNSAFE", "FIELD_NAME_IMPL_LOOKUP");

	@Test
	void shouldNotThrowExceptionIfInitFails() throws Exception {
		for (Field field : MemberAccessorOracleJDK.class.getDeclaredFields()) {
			String fieldName = field.getName();
			// also guard against class manipulators
			if (!fieldName.contains("$") && !EXCLUDED_FIELDS.contains(fieldName) && Modifier.isStatic(field.getModifiers())) {
				Fields.setStaticIgnoreAccess(MemberAccessorOracleJDK.class, fieldName, null);
			}
		}

		MemberAccessorOracleJDK.initialize("________");

		List<Object> staticFieldsValues = new ArrayList<>();
		for (Field field : MemberAccessorOracleJDK.class.getDeclaredFields()) {
			String fieldName = field.getName();
			// also guard against class manipulators
			if (!fieldName.contains("$") && !EXCLUDED_FIELDS.contains(fieldName) && Modifier.isStatic(field.getModifiers())) {
				Object value = Fields.getStaticIgnoreAccess(MemberAccessorOracleJDK.class, fieldName);
				staticFieldsValues.add(value);
			}
		}
		assertTrue(staticFieldsValues.stream().allMatch(Objects::isNull));

		Class<?> sharedSecretsClass = Class.forName(ConstantPoolAccessorOracleJDK.SHARED_SECRETS_CLASS_NAME);
		Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");

		assertThrows(Exception.class, () -> {
			try (MemberAccessorOracleJDK<Method> methodAccessor = new MemberAccessorOracleJDK<>(javaLangAccessGetter)) {
				javaLangAccessGetter.invoke(null);
			}
		});

		MemberAccessorOracleJDK.initialize(MemberAccessorOracleJDK.FIELD_NAME_THE_UNSAFE);
	}

}
