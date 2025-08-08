package se.patrikerdes

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.BuildResult

class VersionsIntegrationFunctionalTest extends BaseFunctionalTest {
    void "the useLatestVersions task won't run if the versions plugin has not been applied"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
            }
        """

        when:
        BuildResult result = useLatestVersionsAndFail()

        then:
        result.output.contains('Task with path \'dependencyUpdates\' not found in root project')
    }

    void "the useLatestVersions task depends on the dependencyUpdates task"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
        """

        when:
        BuildResult result = useLatestVersions()

        then:
        result.task(':dependencyUpdates').outcome == SUCCESS
        result.task(':useLatestVersions').outcome == SUCCESS
    }

    void "the dependencyUpdates task outputs json"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
        """

        when:
        BuildResult result = useLatestVersions()

        then:
        // Path may be prefixed by a local path.
        result.output.contains('Generated report file')
        result.output.contains('report.json')
    }

    void "the useLatestVersionsCheck task depends on the dependencyUpdates task"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':dependencyUpdates').outcome == SUCCESS
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
    }

    void "versions plugin versions"() {
        println("Testing versions plugin version $versionsVersion with gradle version $gradleVersion")
        if (System.getProperty('java.version')[0] == '9' && gradleVersion in GRADLE_VERSIONS_NOT_JDK9) {
            println("Skipping this test on JDK 9, since it does not support gradle version $gradleVersion")
            return
        }

        if (System.getenv('SKIP_SLOW_INTEGRATION_TESTS') != null) {
            println("Found environment variable SKIP_SLOW_INTEGRATION_TESTS, won't test Versions " +
                    "version $versionsVersion")
            return
        }

        if (System.getenv('TRAVIS') != null) {
            println('This test is broken, skipping (TODO: Fix)')
            return
        }

        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$versionsVersion'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation "log4j:log4j:1.2.16"
            }
        """

        when:
        useLatestVersions(gradleVersion)
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("log4j:log4j:$CurrentVersions.LOG4J")

        where:
        versionsVersion | gradleVersion
        '0.52.0'        | '8.14'
     //   '0.20.0'        | '4.9'
     //   '0.19.0'        | '4.8'
      //  '0.18.0'        | '4.7'
      //  '0.17.0'        | '4.6'
      //  '0.16.0'        | '4.2.1'
     //   '0.15.0'        | '4.2.1'
      //  '0.14.0'        | '3.4'
      //  '0.13.0'        | '3.4'
     //   '0.12.0'        | '3.4'
    }
}
