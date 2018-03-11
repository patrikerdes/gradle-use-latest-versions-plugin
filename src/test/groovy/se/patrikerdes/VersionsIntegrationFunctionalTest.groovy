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
}
