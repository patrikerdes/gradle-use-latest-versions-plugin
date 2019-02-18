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
}
