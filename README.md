# Gradle Use Latest Versions Plugin

[![Build Status](https://travis-ci.org/patrikerdes/gradle-use-latest-versions-plugin.svg?branch=master)](https://travis-ci.org/patrikerdes/gradle-use-latest-versions-plugin)

A Gradle plugin that updates module and plugin versions in your *.gradle files to the latest available version.
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
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath 'se.patrikerdes:gradle-use-latest-versions-plugin:0.1.0'
        classpath 'com.github.ben-manes:gradle-versions-plugin:0.17.0'
    }
}

apply plugin: 'com.github.ben-manes.versions'
apply plugin: 'se.patrikerdes.use-latest-versions'
```
