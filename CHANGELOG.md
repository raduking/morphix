## Release Notes

---

`1.0.10`

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


