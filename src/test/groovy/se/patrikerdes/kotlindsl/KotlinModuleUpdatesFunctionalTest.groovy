package se.patrikerdes.kotlindsl

import se.patrikerdes.CurrentVersions

class KotlinModuleUpdatesFunctionalTest extends KotlinBaseFunctionalTest {
    void "an outdated module dependency with a fixed version can be updated, string notation"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile("junit:junit:4.0")
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.JUNIT")
    }

    void "an outdated module dependency with a version range can be updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile("junit:junit:[3.0,4.0]")
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.JUNIT")
    }

    void "a module dependency without a fixed version which is up to date is not updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile("junit:junit:4+")
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('junit:junit:4+')
    }

    void "a variable assigned in a kt file within buildSrc will be updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testCompile("junit:junit:\$junit_version")
                compile("log4j:log4j:\$log4j_version")
            }
        """
        testProjectDir.newFolder('buildSrc', 'src', 'main', 'kotlin')
        File buildSrcBuildGradleFile = testProjectDir.newFile('buildSrc/build.gradle.kts')
        buildSrcBuildGradleFile << '''
            plugins {
                id("org.gradle.kotlin.kotlin-dsl.base") version "1.3.6"
            }
            
            repositories {
                jcenter()
            }
        '''
        File kotlinVersionsFile = testProjectDir.newFile('buildSrc/src/main/kotlin/versions.kt')
        kotlinVersionsFile << '''
            const val junit_version = "4.0"
            const val log4j_version="1.2.16"
        '''

        when:
        useLatestVersions()
        String updatedKotlinVersionsFile = kotlinVersionsFile.getText('UTF-8')

        then:
        updatedKotlinVersionsFile.contains("junit_version = \"$CurrentVersions.JUNIT\"")
        updatedKotlinVersionsFile.contains("log4j_version=\"$CurrentVersions.LOG4J\"")
    }

    void "version delegate and separate, unnamed group, name and version parameter can be updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }
            
            repositories {
                mavenCentral()
            }
            
            val junitVersion: String by project

            dependencies {
                testCompile("junit", "junit", "4.0")
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("""testCompile("junit", "junit", "$CurrentVersions.JUNIT")""")
    }

    void "version delegate and separate, unnamed group, name and version variable can be updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }

            repositories {
                mavenCentral()
            }

            val junitVersion: String by project

            dependencies {
                testCompile("junit", "junit", junitVersion)
            }
        """
        File gradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        gradlePropertiesFile << '''
            junitVersion=4.0
        '''

        when:
        useLatestVersions()
        String updatedGradlePropertiesFile = gradlePropertiesFile.getText('UTF-8')

        then:
        updatedGradlePropertiesFile.contains("junitVersion=$CurrentVersions.JUNIT")
    }

    void "version delegate and separate, named group, name and version parameter can be updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }
            
            repositories {
                mavenCentral()
            }
            
            val junitVersion: String by project

            dependencies {
                testCompile(group = "junit", name = "junit", version = "4.0")
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('testCompile(group = "junit", name = "junit", version = "' +
                CurrentVersions.JUNIT + '")')
    }

    void "version delegate and separate, named group, name and version variable can be updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }

            repositories {
                mavenCentral()
            }

            val junitVersion: String by project

            dependencies {
                testCompile(group = "junit", name = "junit", version = junitVersion)
            }
        """
        File gradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        gradlePropertiesFile << '''
            junitVersion=4.0
        '''

        when:
        useLatestVersions()
        String updatedGradlePropertiesFile = gradlePropertiesFile.getText('UTF-8')

        then:
        updatedGradlePropertiesFile.contains("junitVersion=$CurrentVersions.JUNIT")
    }
}
