# Gradle Use Latest Versions Plugin

[![Build Status](https://travis-ci.org/patrikerdes/gradle-use-latest-versions-plugin.svg?branch=master)](https://travis-ci.org/patrikerdes/gradle-use-latest-versions-plugin)

A Gradle plugin that updates the versions of your dependencies in your *.gradle files to the latest available versions.

This plugin depends on the [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin).

## Usage

Apply this plugin and the [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin).

```groovy
plugins {
  id 'se.patrikerdes.use-latest-versions' version '0.1.0'
  id 'com.github.ben-manes.versions' version '0.17.0'
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
        classpath "gradle.plugin.se.patrikerdes:gradle-use-latest-versions-plugin:0.1.0"
        classpath 'com.github.ben-manes:gradle-versions-plugin:0.17.0'
    }
}

apply plugin: 'com.github.ben-manes.versions'
apply plugin: 'se.patrikerdes.use-latest-versions'

```

## Example

Given this build.gradle file:

```groovy
plugins {
    id 'se.patrikerdes.use-latest-versions' version '0.1.0'
    id 'com.github.ben-manes.versions' version '0.17.0'
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

Your dependencies in build.gradle will be updated - both inline version number and versions based on variables - and you build.gradle file will look like this:

```groovy
plugins {
    id 'se.patrikerdes.use-latest-versions' version '0.1.0'
    id 'com.github.ben-manes.versions' version '0.17.0'
}

apply plugin: 'java'

repositories {
    mavenCentral()
}

ext.log4jversion = '1.2.17'
ext.codecVersion = '1.11'
def commonsLoggingVersion = "1.2"

dependencies {
    testCompile 'junit:junit:4.12'
    compile "log4j:log4j:$log4jversion"
    compile "commons-codec:commons-codec:" + codecVersion
    compile group: 'commons-lang', name: 'commons-lang', version: '2.6'
    compile group: 'commons-logging', name: 'commons-logging', version: commonsLoggingVersion
}
```
