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
package org.morphix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Classes;

/**
 * Test class to verify that all compiled class files have the expected class file version.
 *
 * @author Radu Sebastian LAZIN
 */
class ClassVersionTest {

	private static final String TARGET_CLASSES = "target/classes";

	private static final int JAVA_VERSION = 21;

	private static final int MAGIC = 0xCAFEBABE;
	private static final int MAGIC_VERSION = 44;

	private static int javaVersionToMajor(final int javaVersion) {
		// class file major = 44 + java version
		return MAGIC_VERSION + javaVersion;
	}

	@Test
	void shouldHaveTheCorrectClassVersion() throws Exception {
		Path classesDir = Path.of(TARGET_CLASSES); // compiled classes output
		int expectedMajor = javaVersionToMajor(JAVA_VERSION);

		try (Stream<Path> paths = Files.walk(classesDir)) {
			paths.filter(path -> path.toString().endsWith(Classes.Scan.CLASS_FILE_EXTENSION))
					.forEach(path -> {
						try (DataInputStream in = new DataInputStream(new FileInputStream(path.toFile()))) {
							int magic = in.readInt();
							if (magic != MAGIC) {
								throw new IllegalStateException(path + " is not a valid class file");
							}
							@SuppressWarnings("unused")
							int minor = in.readUnsignedShort();
							int major = in.readUnsignedShort();
							assertEquals(expectedMajor, major, path + " has wrong classfile version");
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					});
		}
	}
}
