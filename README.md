# Gradle Use Latest Versions Plugin

[![Build Status](https://travis-ci.org/patrikerdes/gradle-use-latest-versions-plugin.svg?branch=master)](https://travis-ci.org/patrikerdes/gradle-use-latest-versions-plugin)

A Gradle plugin that updates module and plugin versions in your *.gradle or *.gradle.kts files to the latest available versions.

This plugin depends on the [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin).

## Usage

Apply this plugin and the [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin).

Include in your `build.gradle`

```groovy
plugins {
  id 'se.patrikerdes.use-latest-versions' version '0.2.7'
  id 'com.github.ben-manes.versions' version '0.20.0'
}
```

or

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
        jcenter()
    }
    dependencies {
        classpath "gradle.plugin.se.patrikerdes:gradle-use-latest-versions-plugin:0.2.7"
        classpath 'com.github.ben-manes:gradle-versions-plugin:0.20.0'
    }
}

apply plugin: 'com.github.ben-manes.versions'
apply plugin: 'se.patrikerdes.use-latest-versions'

```

### Usage for Gradle Kotlin DSL

Include in your `build.gradle.kts`
```groovy
plugins {
  id("se.patrikerdes.use-latest-versions") version "0.2.7"
  id("com.github.ben-manes.versions") version "0.20.0"
}
```

or 

```groovy
buildscript {
    repositories {
        maven {
            maven { url = uri("https://plugins.gradle.org/m2/") }
        }
        jcenter()
    }
    dependencies {
        classpath("gradle.plugin.se.patrikerdes:gradle-use-latest-versions-plugin:0.2.7")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.20.0")
    }
}

apply {
    plugin("com.github.ben-manes.versions")
    plugin("se.patrikerdes.use-latest-versions")
}

```

## Example

Given this build.gradle file:

```groovy
plugins {
    id 'se.patrikerdes.use-latest-versions' version '0.2.7'
    id 'com.github.ben-manes.versions' version '0.19.0'
}

apply plugin: 'java'

repositories {
    mavenCentral()
}

ext.log4jversion = '1.2.16'
ext.codecVersion = '1.9'
def commonsLoggingVersion = "1.1.2"

dependencies {
    testCompile 'junit:junit:4.0'
    compile "log4j:log4j:$log4jversion"
    compile "commons-codec:commons-codec:" + codecVersion
    compile group: 'commons-lang', name: 'commons-lang', version: '2.4'
    compile group: 'commons-logging', name: 'commons-logging', version: commonsLoggingVersion
}
```

If you run

```
gradle useLatestVersions
```

Your plugin and module dependencies in build.gradle will be updated – both inline version number and versions based on
variables – and you build.gradle file will look like this:

```groovy
plugins {
    id 'se.patrikerdes.use-latest-versions' version '0.2.7'
    id 'com.github.ben-manes.versions' version '0.20.0' // <- Updated
}

apply plugin: 'java'

repositories {
    mavenCentral()
}

ext.log4jversion = '1.2.17' // <- Updated
ext.codecVersion = '1.11' // <- Updated
def commonsLoggingVersion = "1.2" // <- Updated

dependencies {
    testCompile 'junit:junit:4.12' // <- Updated
    compile "log4j:log4j:$log4jversion" // <- The variable above was updated
    compile "commons-codec:commons-codec:" + codecVersion // <- The variable above was updated
    compile group: 'commons-lang', name: 'commons-lang', version: '2.6' // <- Updated
    compile group: 'commons-logging', name: 'commons-logging', version: commonsLoggingVersion // <- The variable above was updated
}
```

## Tasks

### useLatestVersions

```bash
# gradle useLatestVersions
```

Updates module and plugin versions in all *.gradle files in the project root folder or any subfolder to the latest
available versions. This task depends on the `dependencyUpdates` task in the
[Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin) to know which dependencies can be updated.

### useLatestVersionsCheck

```bash
# gradle useLatestVersions && gradle useLatestVersionsCheck
```

This task will succeed if all available updates were successfully applied by `useLatestVersions`, and it will fail if
any of the updates were not successfully applied. This task depends on the `dependencyUpdates` task in the
[Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin) to know which dependencies were
successfully updated.

`useLatestVersionsCheck` can not run in the same gradle run as `useLatestVersions`, since the `dependencyUpdates` task
will check the *.gradle files as they were when the gradle build started, which means that it can not pick up the
changes applied by `useLatestVersions`.

## Supported dependency formats

Dependencies stated in the following formats should cause the version to be successfully updated by the
`useLatestVersions` task. (If not, please
[create an issue](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/issues/new).)
Single and double quotes are interchangeable in all formats below.

### Plugin dependencies

*Plugin dependencies can only be updated in Gradle 4.4+.*

The plugins DSL only allows a
[strict format](https://docs.gradle.org/current/userguide/plugins.html#sec:constrained_syntax), e.g. only string
literals for the version number, so there is basically only one format to support.

```groovy
plugins {
    id 'se.patrikerdes.use-latest-versions' version '0.1.0'
}
```

### Module dependencies

#### String format

```groovy
dependencies {
    compile "log4j:log4j:1.2.15"
    testCompile 'junit:junit:4.0'
}
```

#### Map format

Currently only if the order is `group`, `name`, `version`, without other elements in between.

```groovy
dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.0'
}
```

### Module dependencies based on variables

`def` and `ext.` (extra properties extensions) can be used interchangeably in all example below.

#### String format
```groovy
ext.junit_version = '4.0'

dependencies {
    testCompile "junit:junit:$junit_version"
}
```

```groovy
def junit_version = '4.0'

dependencies {
    testCompile "junit:junit:${junit_version}"
}
```

```groovy
ext.junit_version = '4.0'

dependencies {
    testCompile "junit:junit:" + junit_version
}
```

#### Map format

Currently only if the order is `group`, `name`, `version`, without other elements in between.

```groovy
ext.junit_version = '4.0'

dependencies {
    testCompile group: 'junit', name: 'junit', version: junit_version
}
```

## Compatibility

**Gradle version:** 2.8 - 4.10.2 (Updating plugin dependencies only work in 4.4+)<br/>
**Versions Plugin version:** 0.12.0 - 0.20.0<br/>
**JDK version:** 7 - 11 (7 is targeted but not tested, 11 is currently not tested but is known to work)

## Instructions for building this plugin from source
* Clone or download this project.
* Open the project, for example in IntelliJ open the `build.gradle` file.
* You can build the jar with the Gradle `assemble` task, it will be in `build/libs/`.
* If you want to use the plugin locally, first publish to your local Maven repository with the Gradle `publishToMavenLocal` task.
* To use it in a different project, add to your `build.gradle` file

```groovy
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies{
        classpath group: 'se.patrikerdes',
				name: 'gradle-use-latest-versions-plugin',
				version: '0.2.7'
    }
}

apply plugin: se.patrikerdes.UseLatestVersionsPlugin

```

## FAQ

#### How do I exclude alpha/beta/rc versions?

The Versions plugin can be configured to achieve this, it is documented in
[the Versions plugin README](https://github.com/ben-manes/gradle-versions-plugin/blob/master/README.md#revisions)

#### Where does the name "Use Latest Versions" come from?

From the [Maven Versions Plugin](http://www.mojohaus.org/versions-maven-plugin/index.html) goal called
[use-latest-versions](http://www.mojohaus.org/versions-maven-plugin/use-latest-versions-mojo.html)

## Changelog

### 0.2.7

Fixed [issue 10](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/issues/10), Multiple versions in gradle.properties ([Balthasar Biedermann](https://github.com/usr42))

[PR #12](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/pull/12), Support dependencySet of Spring Dependency management plugin. ([Balthasar Biedermann](https://github.com/usr42))

[PR #13](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/pull/13), Support classifier and extension in dependencies. ([Balthasar Biedermann](https://github.com/usr42))

### 0.2.6

Fixed [issue 7](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/issues/7), Update version variables in gradle.properties file, again. (0.2.4 didn't fix #7)

### 0.2.5

Fixed [issue 8](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/issues/8), Support for string interpolation with curly braces ${}

### 0.2.4

Fixed [issue 7](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/issues/7), Update version variables in gradle.properties file.

### 0.2.3

Fixed [issue 3](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/issues/3), Output formats are forced to be json,xml. ([Tony Baines](https://github.com/tonybaines))


### 0.2.2

Fixed [issue 2](https://github.com/patrikerdes/gradle-use-latest-versions-plugin/issues/2), Hardcoded Milestone Usage.

### 0.2.1

Added support for Gradle Kotlin DSL build files. ([Thomas Schouten](https://github.com/PHPirates))
