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
package org.morphix.convert.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to annotate destination fields so that the converter knows how to convert it from source.
 *
 * @author Radu Sebastian LAZIN
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Src {

	/**
	 * Source field name.
	 *
	 * @return field name
	 */
	String value() default "";

	/**
	 * Source field name.
	 *
	 * @return field name
	 */
	String name() default "";

	/**
	 * Defines class and name.
	 *
	 * @return from annotation array
	 */
	From[] from() default {};
}
