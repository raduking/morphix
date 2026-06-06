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
package org.morphix.lang;

/**
 * Interface for copyable objects. This interface can be implemented by classes that want to provide a way to create a
 * copy of their instances. This is different from the standard Java {@link Cloneable} interface, as it specifies a
 * method that returns a copy of the object, rather than relying on the clone() method and the associated pitfalls.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Copyable {

	/**
	 * Creates and returns a copy of this object. The exact type of the returned object may be the same as the original or a
	 * different type, depending on the implementation.
	 *
	 * @return a copy of this object
	 */
	Copyable copy();
}
