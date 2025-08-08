package se.patrikerdes.kotlindsl

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.BuildResult
import se.patrikerdes.CurrentVersions

class KotlinCheckFunctionalTest extends KotlinBaseFunctionalTest {
    void "the json file of dependencyUpdates is written by the useLatestVersions task for the check task to consume"() {
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
                testImplementation("junit:junit:4.0")
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
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation("junit:junit:4.0")
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
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }

            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation("junit:junit:4.0")
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
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation("junit:junit:4.0")
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
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
            }

            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation("junit:junit:4.0")
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

    void "useLatestVersionsCheck outputs a special message when there was nothing to update"() {
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
                testImplementation("junit:junit:$CurrentVersions.JUNIT")
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        result.output.contains('useLatestVersions successfully did nothing')
    }
}
