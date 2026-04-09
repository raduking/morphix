## Release Notes

`1.0.30`

- Added `Enums.valueOf(Class, String, Supplier)` which returns the default value supplied if the string cannot be transformed to the enum.
- Added `ExecutionWrapper` functional interface which adds AOP like decorating functionality but without annotations.
- Added `Wait.until` convenience methods for waiting for a specific condition to become true.
- Added `Threads.waitUntil` convenience methods for waiting for a specific condition to become true which all delegate to `Wait.until`.

---

`1.0.29`

- Fixed `ConcurrentThreadLRUCache` eviction method to acquire lock always.

---

`1.0.28`

- Added `ScopedResource.closeIfManaged(AutoClosable)` to be able to provide a custom `close` function.
- Added `ScopedResource.get` as an alias for `ScopedResource.unwrap`.
- Added `ScopedResource.derive` to create child scoped resources with the same management/ownership as the parent.
- Added `ExceptionThrowingFunction` similar to `ThrowingFunction` throwing `Exception` instead of `Throwable`.
- Moved `Pair` from `apiphany` to `morphix`.
- Moved `Holder` from `apiphany` to `morphix`.
- Added `OptionalLibrary` class to describe a library support (moved and renamed from `org.apiphany.lang.LibraryDescriptor`).
- Added `Libraries` utility interface to create the proper instance for the available library in the classpath (from apiphany `LibraryInitializer`).

---

`1.0.27`

- Changed `Threads.waitUntil` methods to return true if the condition was met, false if timeout reached.
- Moved `lang.accumulator` package from `apiphany` to `morphix`.
- Moved `lang.collections` package from `apiphany` to `morphix`.
- Moved `lang.retry` package from `apiphany` to `morphix`.
- Moved `Temporals` class from `apiphany` to `morphix`.
- Added `ResourceLeakTracker.isClosed` to check if the resource leak tracker is closed.
- Added support for leak message hints to `ResourceLeakDetector`.
- Added resource leak tracking to `ScopedResource`.
- Removed `org.morphix.*` classes from resource leak call site frames since they are useless.

---

`1.0.26`

- Updated `Case.capitalize` to use `Locale.ROOT`.
- Added `Case.capitalize(String, Locale)` to be able to capitalize a word with the given `Locale`.
- Added `Cache` interface to allow for cache implementations.
- Added `LRUCache` interface which extends the `Cache` interface to allow for least recently used (LRU) cache implementations.
- Added `StrictLRUCache` as a LRU cache implementation with O(1) operations, this implementation is not thread safe.
- Added `ConcurrentStrictLRUCache` as a thread safe strict LRU cache.
- Upgraded `pitest-maven` to `1.23.0`.
- Upgraded `properties-maven-plugin` to `1.3.0`.
- Upgraded parent to `4.0.5`.
- Added `org.morphix.lang.leak` package which adds a resource leak detection framework.
- Added `LeakDetectionLevel` enumeration to configure leak detection via the system property named `morphix.leak.detection.level`.
- Added `ResourceLeakDetector` class with resource leak detection via `ResourceLeakTracker`.
- Added `ResourceLeakTracker` class to track leaks for a resource.
- Added `ResourceLeakReporter` interface that defines leak reporting.
- Added `ResourceLeakLogger` class that implements `ResourceLeakReporter` using JUL (java.util.logging).
- Moved `Lifecycle` from `apiphany` here since it's general purpose.
- Moved `ScopedResource` from `apiphany` here since it's general purpose.
- Added `ScopedResource.close` since it now implements `AutoCloseable`.
- Added `Threads.isCurrentInterrupted` to check if the current thread is interrupted.
- Added `Threads.waitUntil` methods to be able to wait for a condition to be true.
 
---

`1.0.25`

- Added `ThrowingUnaryOperator` similar to `ThrowingFunction`.
- Added `Ids` class with utility methods to generate, convert, transform `UUID`s to/from `BigInteger`s.
- Added `Ids.UUIDStyle` to specify the `UUID` style, currently `NO_DASHES`, `UPPERCASE` and `LOWERCASE`.
- Upgraded parent to `4.0.3`.
- Upgraded `central-publishing-maven-plugin` to `0.10.0`.
- Upgraded `spotless-maven-plugin` to `3.3.0`.
- Upgraded `pitest-maven` to `1.22.1`.
- Upgraded JUnit to `6.0.3`.

---

`1.0.24`

- Added `Suppliers.safeGet(Supplier, Supplier)` static method to safely get the supplied value or return a default value.
- Added `Suppliers.safeGet(Supplier)` static method to safely get the supplied value or return `null`.
- Added `MapConversions.toPropertiesMap(Object, SimpleConverter)` with property name conversion to be able to convert to snake case for example.
- Added property name converter to the `PropertyConversionEngine`.
- Added `PropertyConversionEngine.getPropertyNameConverter` to retrieve the property name converter.
- Added `PropertyConversionEngine.getDefaultPropertyNameConverter` to retrieve the default property name converter.
- Added `Case` enum with string converters between different cases `LOWER_CAMEL`, `SNAKE`, `KEBAB`, etc.

---

`1.0.23`

- Renamed `FieldHandlerResult.SKIP` to `SKIPPED`.
- Renamed `FieldHandlerResult.BREAK` to `HANDLED`.
- Added `FieldHandlerContext` to transmit information between field handlers.
- Added `Methods.getAllDeclared(Class, Predicate)` to return all declared methods for a given class with a given predicate.
- Added `StackedContextHolder` abstract class with a thread local stack that provides the appropriate context even in recursive calls.
- Added `ThreadContextException` exception for thread context errors.
- Renamed `HandleMethods` to `Handles`.

---

`1.0.22`

- Renamed `AnyFromMap` field handler to `MapToAny`.
- Added map conversions as part of converter handler chain so that map conversions can be done by default.
- Added `MapConversions.toPropertiesMap(Object)` method that deep converts an object to a map of field names and values with support for cyclic references.
- Renamed `Configuration.defaultConfiguration` to `Configuration.defaults`.
- Added `ConversionContext` to track cyclic references during conversions.
- Renamed `MethodType.getPredicate` to `MethodType.predicate`.
- Renamed `ConversionStrategy` to `FieldFinderStrategy` to better suit its intent.
- Renamed all finder strategies, they were/are meant as internal classes they all implement `ConversionStrategy`.
- Added strategy based implementation to `MapConversions.toPropertiesMap`.
- Added `ExtendedFields` interface with utility methods for `ExtendedField`.
- Moved `FieldFinderStrategy.findFields` to `ExtendedFields.findAllNonStatic`.
- Added `Optional` fields support to object converter.
- Added `MethodPredicates.hasSignature` as an alias for `isMethodWith`.
- Added `FieldPredicates` utility class for `Field` related predicates.

---

`1.0.21`

- Added `JavaArrays` utility interface with utility methods for Java arrays.
- Added `Nullables.whenNotNull` alias methods to all `Nullables.apply` methods.
- Added `Fields.getAllDeclared(Object)` so that the caller doesn't need to call `getClass` everywhere.
- Added `Fields.getAllDeclared(Class, Predicate)` to return all fields from a class matching a predicate.
- Added `Fields.getAllDeclared(Object, Predicate)` to return all fields from an object matching a predicate.
- Added `Fields.getAllDeclaredInHierarchy(Object, Predicate)` so that the caller doesn't need to call `getClass` everywhere.
- Added `ExtendedField.getAnnotation` to return the annotation from an extended field.
- Added `ToStringFunction` for generalizing the `Object.toString`.
- Upgraded to JUnit 6 (version `6.0.2`).
- Upgraded parent to `4.0.2`.

---

`1.0.20`

- Added `Constructors.Safe` name space interface for methods that return `null` instead of throwing exceptions.
- Added `Constructors.Safe.getDefault` to return the default constructor if it is declared, `null` otherwise.
- Added `Constructors.Safe.getDeclared` to return a declared constructor if it is declared, `null` otherwise.

---

`1.0.19`

- Upgraded formatter version to `4.38`.
- Upgraded `spotless-maven-plugin` to `3.1.0`.
- Upgraded `org.eclipse.jdt/org.eclipse.jdt.core` to `3.44.0`.
- Added `code-format` version `1.0.0` as the source for Java formatter file `java-code-style.xml`.
- Added `Nullables.nonNullOrThrow` to throw the supplied `Throwable` instead of using `nonNullOrDefault`.
- Added `Predicates.alwaysFalse` to return a predicate that is always false.
- Added `Predicates.rejectAll` to return a predicate that is always false (alias to `alwaysFalse`).
- Added `Predicates.acceptAll` to return a predicate that is always true (alias to `alwaysTrue`).
- Added `Suppliers` class with utility methods for suppliers.
- Added `Suppliers.supplyTrue` to always supply `true`.
- Added `Suppliers.supplyFalse` to always supply `false`.
- Added `Suppliers.supplyNull` to always return `null`.
- Added `Suppliers.supplyNull(Runnable)` which returns a supplier that runs the runnable and then returns `null`.
- Added `Suppliers.compose(Runnable, Supplier)` which returns a supplier that runs the runnable and the returns the supplied value by the given supplier.
- Added `IntPredicates.alwaysTrue` to return a predicate that is always false.
- Added `IntPredicates.alwaysFalse` to return a predicate that is always false.
- Added `IntPredicates.rejectAll` to return a predicate that is always false (alias to `alwaysFalse`).
- Added `IntPredicates.acceptAll` to return a predicate that is always true (alias to `alwaysTrue`).
- Added internal lambda constants for all these methods so that Java doesn't create instances for each occurrence.

---

`1.0.18`

- Moved `PutFunction` to `lang.function` package.
- Added `PutFunction.of` factory method.
- Added `PutFunction.compose` to compose two map put functions.
- Added `PutFunction.ifNotNullValue` to return a put function that only performs the `put` if the value provided is not `null`.
- Added `PutFunction.ifNotNullKey` to return a put function that only performs the `put` if the key provided is not `null`.
- Added `PutFunction.ifNotNullKeyAndValue` to return a put function that only performs the `put` if the key and value provided is not `null`.
- Changed `SimpleConverter` to extend `Function`.
- Changed `ConverterFunction` to extend `BiConsumer`.
- Fixed `ArrayConversionPipeline.to(Object[])` to properly convert when the destination array is smaller than the source array.
- Added `ArrayConversionPipeline.toArray(IntFunction)` to match Java collections style.

---

`1.0.17`

- Upgraded parent to `3.5.8`.
- Added `GenericType.Arguments` helper interface to build the generic type arguments.
- Added `GenericType.getGenericParameterType(ParameterizedType, int)` with `ParameterizedType` as the first parameter.
- Added `GenericType.getGenericParameterType(ParameterizedType, String, int)` with `String` as the second as the type name.
- Added `Messages` class with utility methods for lightweight message interpolation for diagnostics and exceptions.
- Added `Messages.message` method which replaces each `{}` placeholder in the template with the corresponding argument.
- Added `ReflectionException` constructors with template messages using the `Messages.message` method internally.
- Added `MapConversions.convertToMap` static method for a simple conversion of an object to a map.
- Added `PutFunction` functional interface to be able to pass `Map::put` method references.

---

`1.0.16`

- Added `GenericClass.of` method without parameters to allow instantiation without a type set (null type).

---

`1.0.15`

- Added class file version test to ensure correct class files are built.
- Added `JavaClassFile` interface for class files utility methods (it's not named `ClassFile` so it does not clash with the Java 25 class with the same name).
- Added `JavaClassFile.Version` record which represents a class file version.
- Moved and renamed `Classes.CLASS_FILE_EXTENSION` constant to `JavaClassFile.EXTENSION`.
- Added `maven-properties-plugin` to `pom.xml` to output all Maven properties to `target/maven.properties`.
- Added `Methods.getOneDeclared` which just delegates to `Methods.Safe.getOneDeclared`.
- Added `Methods.invoke` to invoke accessible methods contrary to `Methods.IgnoreAccess.invoke`.
- Fixed `GenericClass` accepting non generic classes as generic type arguments.
- Renamed `Constructors.getDefaultConstructor` to `Constructors.getDefault`.
- Renamed `Constructors.getDeclaredConstructor` to `Constructors.getDeclared`.

---

`1.0.14`

- Added `Annotations` utility class to handle annotations.
- Added `Annotations.overrideValue` to override an annotations' value at runtime.
- Changed `Methods.getOneDeclaredInHierarchy` to return `null` instead of throwing an exception when the method is not found.
- Added `Classes.Scan` name space class for class finder methods.
- Added `Classes.Scan.findInPackage` method to find classes in a given package.
- Added `Classes.Scan.findInDirectory` method to find classes in a given directory.
- Added `Classes.Scan.findWithAnyAnnotation` methods to find classes with a given set of annotations.

---

`1.0.13`

- Changed `Nullables.nonNullList` to return an unmodifiable list.
- Added `Methods.getAllDeclared` that returns a `List` with all declared methods in a given class.
- Added `Methods.Complete` name-space interface for utility methods that return all methods including interface methods.
- Added `Methods.Complete.getAllDeclaredInHierarchy(Class)` method that returns all methods including interfaces.
- Added `Methods.Complete.getAllDeclaredInHierarchy(Class, Set)` method that returns all methods including interfaces that can exclude classes/interfaces.
- Added `Classes.mutableSetOf` method to create a mutable set of classes.

---

`1.0.12`

- Fixed wrong order for `Methods.getAllDeclaredInHierarchy(Class, Predicate)`.
- Changed `Comparables` constructor to throw `UnsupportedOperationException` to prevent instantiation.
- Upgraded `pitest-maven` to `1.22.0`.
- Upgraded `jacoco-maven-plugin` to `0.8.14`.
- Upgraded `maven-gpg-plugin` to `3.2.8`.
- Upgraded `central-publishing-maven-plugin` to `0.9.0`.
- Moved `Reflection.findSubclass` method to `Classes`.
- Added `Serializable` to `ObjectConverter` to allow serialization of converters.
- Added `Nullables.Chain.thenNotNull` to chain null checks.

---

`1.0.11`

- Added `Methods.getCallerMethodName` without a supplier to get the caller method name.

---

`1.0.10`

- Added `Fields.getOneDeclared` to retrieve a declared field in a given class.
- Added `Methods.Safe` name space for methods that return null on expected errors (exceptions).
- Renamed and moved `Methods.getSafeOneDeclaredInHierarchy` method to `Methods.Safe.getOneDeclaredInHierarchy`.
- Renamed and moved `Methods.getSafeGenericReturnType` method to `Methods.Safe.getGenericReturnType`.
- Added `Methods.Safe.getOneDeclared` methods to return a declared method from a class or an object.
- Added `Classes` class with reflection utility methods for classes.
- Removed `Reflection.getClass` in favor of `Classes.Safe.getOne` since it does the same thing.
- Added Java record support for `MethodType`.
- Added `BinaryOperators` utility class for common `BinaryOperator`s.
- Changed `ConversionStrategy.find` method to have the source field list parameter if the list is available for faster searches.
- Removed many stream operations from the converter for faster conversions. 

---

`1.0.9`

- Added `GenericType.isNotGenericClass` which returns true if the given class is not generic, false otherwise.
- Added `GenericType.of(Class, Type, Type)` which creates a generic type with only one generic argument.
- Added `GenericType.of(Class, Type)` which creates a generic type with only one generic argument with `null` as the owner type.
- Added `Comparables` class for utility methods for `Comparable` objects.
- Moved `JavaObjects.max` method to `Comparable` class.
- Moved `JavaObjects.min` method to `Comparable` class.
- Changed some methods to use the Java 21 switch pattern matching for improved readability.

---

`1.0.8`

- Added `Reflection.isClassPresent` to test if a class is present in the classpath.

---

`1.0.7`

- Renamed all `Fields` methods to follow the pattern `getAll*` for methods returning lists and `getOne*` for methods returning single fields.
- Renamed all `Methods` methods to follow the pattern `getAll*` for methods returning lists and `getOne*` for methods returning single methods.
- Renamed all `HandleMethods` methods to not contain the word "Method" anymore for brevity.

---

`1.0.6`

- Changed `Fields.IgnoreAccess.get` with field name as parameter to look for the field in the class hierarchy.
- Changed `Fields.IgnoreAccess.set` with field name as parameter to look for the field in the class hierarchy.
- Changed `Fields.IgnoreAccess.getStatic` with field name as parameter to look for the field in the class hierarchy because they are accessible through derived classes too.
- Changed `Fields.IgnoreAccess.setStatic` with field name as parameter to look for the field in the class hierarchy because they are accessible through derived classes too.
- Added `ThrowingBiFunction` to wrap a `BiFunction` that throws without the need for a `try`/`catch`.
- Added `MemberPredicates.isAbstract` to test if a member is abstract.
- Added `HandleMethods` utility class for faster method invoking using method handles.
- Added `Methods.getFunctionalInterfaceMethod` to find the functional interface method for a class/interface.
- Added `MethodSignature` record to hold a method signature for invocation caching.
- Changed all methods in `Methods` and `Fields` classes to use declared generic arguments instead of a wild card.
- Renamed `Fields.resetField` to `Fields.reset` and inverted parameters.
- Added support for setting private final fields.
- Renamed `Fields.setFieldValue` to `Fields.set`.
- Renamed `Fields.getFieldValue` to `Fields.get`.

---

`1.0.5`

- Upgraded parent to `3.5.3`.
- Moved `Methods.invokeMethodsWithAnnotation` to `Methods.IgnoreAccess.invokeWithAnnotation`.
- Moved `Methods.invokeWithOriginalException` to `Methods.IgnoreAccess.invokeWithOriginalException`.

---

`1.0.4`

- Updated dependencies to latest versions.
- Changed `GenericType.of` method to throw `ReflectionException` when the parameter is not of type `ParameterizedType`.
- Changed `GenericClass` to be mutable so that the generic argument type can be set dynamically.
- Upgraded parent to `3.5.0`.

---

`1.0.3`

- `Reflection.findSubclass` no longer throws `IllegalStateException` it throws `ReflectionException` instead.
- Added `Nullables.requireNull` for easier preconditions throwing an `IllegalArgumentException` if the parameter is not `null`.
- Added `JavaObjects.max` that returns the maximum of two `Comparable`s
- Added `JavaObjects.min` that returns the minimum of two `Comparable`s
- Updated parent to `3.4.5`
- Added `Methods.getCallerMethodName` with skipFrames parameter.
- Added `Consumers` class with utility methods for consumers.
- Added `Consumers.noBiConsumer` method that returns an empty `BiConsumer`.
- Added `Runnables` class with utility methods for runnables.
- Removed consumer and runnable methods from `Threads` class.

---

`1.0.2`

- First official release.

---


