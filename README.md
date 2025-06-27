# MorphiX

A library that exposes general Java utility and reflection methods and a package for converting a *source* object to a *destination* object.

#### Languages and Tools
<p>
	<a href="https://www.java.com" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="java" width="40" height="40"/></a>
</p>

### License

[Apache License, Version 2.0](LICENSE)

### Why MorphiX?

#### Reflection

- Provides utility classes / objects / methods for easier development with Java reflection.

#### Conversions

- Provides an API for converting a source java object to a destination Java object.
- It's simple, fast and has no byte-code manipulation
- It has no outside dependencies, built entirely on standard Java 21+
- No annotation processing needed
- Uses reflection (fields + getters/setters)
- Iterates through destination object fields
- Ignores access modifiers
- Uses conventions for converting

### Releases

Current release `1.0.6`

### Documentation

- [Simple conversions](doc/simple.md)
- [Enums](doc/enums.md)
- [Auto conversions](doc/auto-conversions.md)

### Examples

See [examples](src/test/java/org/morphix/examples) folder in unit tests for examples.

### Getting Started

For the latest development release, you need to get the [develop](https://github.com/raduking/morphix/tree/develop) branch and build it yourself:

```
git clone git@github.com:raduking/morphix.git
cd morphix
mvn clean install
```

Maven: add this dependency to your `pom.xml`

```xml
<dependency>
    <groupId>io.github.raduking</groupId>
    <artifactId>morphix-all</artifactId>
    <version>1.0.6</version>
</dependency>
```
