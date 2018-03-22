package se.patrikerdes

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.BuildResult

class CheckFunctionalTest extends BaseFunctionalTest {
    void "the json file of dependencyUpdates is written by the useLatestVersions task for the check task to consume"() {
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
        File useLatestVersionsPath = new File(new File(testProjectDir.root, 'build'), 'useLatestVersions')
        File jsonReport = new File(useLatestVersionsPath, 'latestDependencyUpdatesReport.json')

        then:
        useLatestVersionsPath.exists()
        jsonReport.exists()
        jsonReport.length() > 500  // Was 859 when the test was developed
    }

    void "the check task fails if useLatestVersions task has not run"() {
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
        BuildResult result = useLatestVersionsCheckAndFail()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains('No results from useLatestVersions were found, aborting')
    }

    void "the check task fails if clean has run between useLatestVersions and useLatestVersionsCheck"() {
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
        clean()
        BuildResult result = useLatestVersionsCheckAndFail()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains('No results from useLatestVersions were found, aborting')
    }

    void "useLatestVersionsCheck is successful if it runs after useLatestVersions"() {
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
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
    }

    void "useLatestVersionsCheck outputs success if all updates were successful"() {
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
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        result.output.contains('successfully updated 1 dependency')
        result.output.contains(" - junit:junit [4.0 -> $CurrentVersions.JUNIT]")
    }

    void "useLatestVersionsCheck fails if any update failed"() {
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
            
            def junit_version = '3.0'
            junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheckAndFail()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains('failed to update 1 dependency')
        result.output.contains(" - junit:junit [4.0 -> $CurrentVersions.JUNIT]")
    }

    void "useLatestVersionsCheck correctly prints one successful and one unsuccessful update"() {
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
            
            def junit_version = '3.0'
            junit_version = '4.0'
            
            dependencies {
                compile "log4j:log4j:1.2.16"
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheckAndFail()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains("""\
            useLatestVersions failed to update 1 dependency to the latest version:
             - junit:junit [4.0 -> $CurrentVersions.JUNIT]
            useLatestVersions successfully updated 1 dependency to the latest version:
             - log4j:log4j [1.2.16 -> $CurrentVersions.LOG4J]
        """.stripIndent())
    }

    void "useLatestVersionsCheck outputs a special message when there was nothing to update"() {
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
                testCompile 'junit:junit:$CurrentVersions.JUNIT'
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        result.output.contains('useLatestVersions successfully did something nothing')
    }
}
