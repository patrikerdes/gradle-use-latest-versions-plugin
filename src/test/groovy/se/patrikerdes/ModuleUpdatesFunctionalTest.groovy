package se.patrikerdes

class ModuleUpdatesFunctionalTest extends BaseFunctionalTest {
    void "an outdated module dependency with a fixed version can be updated, string notation"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile 'junit:junit:4.0'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.JUNIT")
    }

    void "an outdated module dependency with a fixed version can be updated, map notation, single quotes"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
            
            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile group: 'junit', name: 'junit', version: '4.0'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("group: 'junit', name: 'junit', version: '$CurrentVersions.JUNIT'")
    }

    void "an outdated module dependency with a fixed version can be updated, map notation, double quotes"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile group: "junit", name: "junit", version: "4.0"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("group: \"junit\", name: \"junit\", version: \"$CurrentVersions.JUNIT\"")
    }

    void "an outdated module dependency with a version range can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
            
            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile 'junit:junit:[3.0,4.0]'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('junit:junit:4.12')
    }

    void "a module dependency without a fixed version which is up to date is not updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
            
            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile 'junit:junit:4+'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('junit:junit:4+')
    }

    void "outdated module dependencies in multiple build files can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
            
            apply plugin: 'java'
            
            apply from: 'second.gradle'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile 'junit:junit:4.0'
            }
        """
        File secondFile = testProjectDir.newFile('second.gradle')
        secondFile << """
            dependencies {
                compile 'log4j:log4j:1.2.16'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')
        String updatedSecondFile = secondFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('junit:junit:4.12')
        updatedSecondFile.contains("log4j:log4j:$CurrentVersions.LOG4J")
    }

    void "spring gradle dependency management plugin annotation with variable"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
                id "io.spring.dependency-management" version "1.0.6.RELEASE"
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencyManagement {
                dependencies {
                    dependency "junit:junit:4.0"
                    dependencySet(group: 'log4j', version: "1.2.16") {
                        entry 'log4j'
                    }
                }
            }
            
            dependencies {
                testCompile "junit:junit"
                compile "log4j:log4j"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("dependency \"junit:junit:$CurrentVersions.JUNIT\"")
        updatedBuildFile.contains("dependencySet(group: 'log4j', version: \"$CurrentVersions.LOG4J\")")
    }
}
