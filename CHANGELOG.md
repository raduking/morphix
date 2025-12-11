## Release Notes

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

- Added `Reflection.isClassPresent` to test if a class is present in the class path.

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


