package se.patrikerdes

class ModuleUpdatesFunctionalTest extends BaseFunctionalTest {
    def "an outdated module dependency with a fixed version can be updated, string notation"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
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
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.junit")
    }

    def "an outdated module dependency with a fixed version can be updated, map notation, single quotes"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
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
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("group: 'junit', name: 'junit', version: '$CurrentVersions.junit'")
    }

    def "an outdated module dependency with a fixed version can be updated, map notation, double quotes"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
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
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("group: \"junit\", name: \"junit\", version: \"$CurrentVersions.junit\"")
    }

    def "an outdated module dependency with a version range can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
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
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('junit:junit:4.12')
    }

    def "a module dependency without a fixed version which is up to date is not updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
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
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('junit:junit:4+')
    }

    def "outdated module dependencies in multiple build files can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
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
        def updatedBuildFile = buildFile.getText('UTF-8')
        def updatedSecondFile = secondFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('junit:junit:4.12')
        updatedSecondFile.contains('log4j:log4j:1.2.17')
    }
}
