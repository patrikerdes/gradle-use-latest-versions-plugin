package se.patrikerdes

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class VersionsIntegrationFunctionalTest extends BaseFunctionalTest {
    def "the useLatestVersions task won't run if the versions plugin has not been applied"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
            }
        """

        when:
        def result = useLatestVersionsAndFail()

        then:
        result.output.contains('Task with path \'dependencyUpdates\' not found in root project')
    }

    def "the useLatestVersions task depends on the dependencyUpdates task"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
            }
        """

        when:
        def result = useLatestVersions()

        then:
        result.task(":dependencyUpdates").outcome == SUCCESS
        result.task(":useLatestVersions").outcome == SUCCESS
    }

    def "the dependencyUpdates task outputs json"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
            }
        """

        when:
        def result = useLatestVersions()

        then:
        result.output.contains('Generated report file build/dependencyUpdates/report.json')
    }

    def "the useLatestVersionsCheck task depends on the dependencyUpdates task"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
            }
        """

        when:
        useLatestVersions()
        def result = useLatestVersionsCheck()

        then:
        result.task(":dependencyUpdates").outcome == SUCCESS
        result.task(":useLatestVersionsCheck").outcome == SUCCESS
    }

    def "versions plugin versions"() {
        println("Testing versions plugin version $versionsVersion with gradle version $gradleVersion")
        if(System.getProperty("java.version")[0] == "9" && gradleVersion in unsupportedGradleVersionsJDK9) {
            println("Skipping this test on JDK 9, since it does not support gradle version $gradleVersion")
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
                testCompile 'junit:junit:4.0'
            }
        """

        when:
        useLatestVersions(gradleVersion)
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.junit")

        where:
        versionsVersion | gradleVersion
        '0.17.0'        | '4.6'
        '0.16.0'        | '4.2.1'
        '0.15.0'        | '4.2.1'
        '0.14.0'        | '3.4'
        '0.13.0'        | '3.4'
        '0.12.0'        | '3.4'
    }
}
