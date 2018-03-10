package se.patrikerdes

class VariableUpdatesFunctionalTest extends BaseFunctionalTest {
    def "an outdated module dependency based on a variable can be updated, single quotes"() {
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
            
            def junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = '$CurrentVersions.junit'")
    }

    def "an outdated module dependency based on a variable can be updated, double quotes"() {
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
            
            def junit_version = "4.0"
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = \"$CurrentVersions.junit\"")
    }

    def "Extra properties extensions can be updated"() {
        // Taken from https://kotlinlang.org/docs/reference/using-gradle.html
        given:
        buildFile << """
            buildscript {
                ext.kotlin_version = '1.2.21'
            
                repositories {
                    mavenCentral()
                }
            
                dependencies {
                    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:\$kotlin_version"
                }
            }

            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
            }

            apply plugin: 'kotlin'
        """

        when:
        useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("ext.kotlin_version = '$CurrentVersions.kotlin'")
    }

    def "a variable used for multiple dependencies with different latest versions won't be updated"() {
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
            
            def commons = "1.0"
            
            dependencies {
                compile "commons-lang:commons-lang:\$commons"
                compile "commons-logging:commons-logging:\$commons"
            }
        """

        when:
        def result = useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('commons = "1.0"')
        result.output.contains("A problem was detected")
    }

    def "a variable used for multiple dependencies with different latest versions where one dependency is at its latest version won't be updated"() {
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
            
            def commons = "1.2"
            
            dependencies {
                compile "commons-codec:commons-codec:\$commons"       // Latest version: 1.2
                compile "commons-logging:commons-logging:\$commons"   // Latest version: 1.11
            }
        """

        when:
        def result = useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('commons = "1.2"')
        result.output.contains("A problem was detected")
    }

    def "a variable defined more than once won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
            }

            def junit_version = '3.7'

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        def result = useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        !updatedBuildFile.contains("junit_version = '$CurrentVersions.junit'")
        result.output.contains("A problem was detected")
    }

    def "a variable assigned to in more than one file won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
            }

            project.ext.junit_version = '3.7'

            apply from: 'second.gradle'

            apply plugin: 'java'

            repositories {
                mavenCentral()
            }

            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """
        File secondFile = testProjectDir.newFile('second.gradle')
        secondFile << """
            rootProject.junit_version = '4.0'
        """

        when:
        def result = useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')
        def updatedSecondFile = secondFile.getText('UTF-8')

        then:
        !updatedBuildFile.contains("kotlin_version = '$CurrentVersions.kotlin'")
        !updatedSecondFile.contains("kotlin_version = '$CurrentVersions.kotlin'")
        result.output.contains("A problem was detected")
    }
}
