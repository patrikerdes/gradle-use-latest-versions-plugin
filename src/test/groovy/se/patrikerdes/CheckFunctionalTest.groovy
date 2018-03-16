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

    void "useLatestVersionsCheck outputs success if there is nothing left to update after useLatestVersions"() {
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
        result.output.contains('successfully upgraded all dependencies')
    }

    void "useLatestVersionsCheck fails if there is anything left to update after useLatestVersions"() {
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
        result.output.contains('failed to update at least one dependency')
    }
}
