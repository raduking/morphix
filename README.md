# MorphiX

##### A framework that exposes generic methods for converting a *source* object to a *destination* object.

#### Languages and Tools
<p>
	<a href="https://www.java.com" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="java" width="40" height="40"/></a>
</p>

### Why MorphiX?

- Simple
- Fast
- Easy to add new functionality
- Thread safe
- No bytecode manipulation
- No outside dependencies, built entirely on standard Java 8+

### How does it work?

- Uses reflection (fields + getters/setters)
- Iterates throgh destination object fields
- Ignores access modifiers
- Uses common conventions for converting

### Conventions

- No conversion for fields with the same type (direct assignment)
- Strings to Enums
- Boxing - Auto-boxing
- Strings to Any via static methods
- Any to Any via constructor
- Any to String via toString()
- Any from conversion function
- Iterables to Iterables
- Maps to Maps
- Arrays to Arrays

### Releases

Current release `1.0.0`

### Documentation

- [Simple conversions](doc/simple.md)
- [Enums](doc/enums.md)
- [Auto conversions](doc/auto-conversions.md)

### Examples

See [examples](src/test/java/org/morphix/examples) folder in unit tests for examples.

### Getting Started

Maven: add this dependency to your `pom.xml`

```xml
<dependency>
	<groupId>org.morphix</groupId>
	<artifactId>morphix</artifactId>
	<version>latest</version>
</dependency>
```

Replace `latest` with the latest one.
