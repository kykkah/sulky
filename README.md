# sulky Modules [![Build Status](https://travis-ci.com/huxi/sulky.png?branch=master)](https://travis-ci.com/huxi/sulky) [![Coverage Status](https://coveralls.io/repos/huxi/sulky/badge.png)](https://coveralls.io/r/huxi/sulky) [![Maven Central](https://img.shields.io/maven-central/v/de.huxhorn.sulky/de.huxhorn.sulky.ulid.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.huxhorn.sulky%22)

sulky is the umbrella project for several general purpose modules used by Lilith.
In contrast to Lilith, sulky modules are licensed LGPLv3 & ASLv2. 

## Build

- Requires Java 17 (toolchains configured). Recommended to run Gradle with JDK 17.
- On macOS: `export JAVA_HOME=$(/usr/libexec/java_home -v 17)` then run `./gradlew build`.
- Gradle will auto-download a JDK 17 toolchain for compilation if needed.

Benchmarking (JMH)
- JMH plugin is disabled by default to keep plugin resolution lean. Enable when needed with `./gradlew -PenableJmh :sulky-ulid:jmh`.
