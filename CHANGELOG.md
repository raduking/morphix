## Release Notes

---

`1.0.6`

- Changed `Fields.IgnoreAccess.get` with field name as parameter to look for the field in the class hierarchy.
- Changed `Fields.IgnoreAccess.set` with field name as parameter to look for the field in the class hierarchy.
- Changed `Fields.IgnoreAccess.getStatic` with field name as parameter to look for the field in the class hierarchy because they are accessible through derived classes too.
- Changed `Fields.IgnoreAccess.setStatic` with field name as parameter to look for the field in the class hierarchy because they are accessible through derived classes too.
- Added `ThrowingBiFunction` to wrap a `BiFunction` that throws without the need for a `try`/`catch`.

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


