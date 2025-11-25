# Build

The following instructions explain how to build KeepTime from sources.

## Prerequisites

* JDK with JavaFx
  * 17 ✅
  * 21 ✅
  * 23 ✅
  * 25 ❗ (Compatible for building without test)
* Maven

Amazon Corretto JDK has JavaFx Bundled.  
For other JDKs you may have to install JavaFx separately.

The Mockito mocking library version in use for tests is not compatible with Java 25.  
If you trust us and you don't need to run test but you just want to build from the stable `main` branch, Java 25 is supported.

## Build Commands

Run one of the following commands from the root directory of this project, where the `pom.xml` file is found.

A zip file and a jar with dependencies bundled with it will be created at the `target` directory.  
If you don't use the profile `coverage`, crutial parts of the application will not be built.

### Build Without Tests

`mvn package -P coverage -D maven.test.skip true`

Only recommended on the stable `main` branch.

### Build With Tests

`mvn package -P coverage`

### Build With Coverage Reports

`mvn package org.jacoco:jacoco-maven-plugin:0.8.7:prepare-agent org.jacoco:jacoco-maven-plugin:0.8.7:report -P coverage`
