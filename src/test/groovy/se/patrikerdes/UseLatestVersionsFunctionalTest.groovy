import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class UseLatestVersionsFunctionalTest extends Specification {
    public static final String basicBuildFile = """
        plugins {
            id 'se.patrikerdes.use-latest-versions'
            id 'com.github.ben-manes.versions' version '0.17.0'
        }
    """
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "the task can not run if the versions plugin has not been applied"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
            }
        """

        when:
        def result = buildAndFail()

        then:
        result.output.contains('Task with path \'dependencyUpdates\' not found in root project')
    }

    def "if the versions plugin has been applied, run the dependencyUpdates and useLatestVersions tasks"() {
        given:
        buildFile << basicBuildFile

        when:
        def result = build()

        then:
        result.task(":dependencyUpdates").outcome == SUCCESS
        result.task(":useLatestVersions").outcome == SUCCESS
    }

    def "the dependencyUpdates task outputs json"() {
        given:
        buildFile << basicBuildFile

        when:
        def result = build()

        then:
        result.output.contains('Generated report file build/dependencyUpdates/report.json')
    }

    def "an outdated module dependency can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '0.17.0'
            }
            
            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile 'org.assertj:assertj-core:3.9.0'
            }
        """

        when:
        def result = build()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('org.assertj:assertj-core:3.9.1')
    }

    private BuildResult build() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersions')
                .withPluginClasspath()
                .build()
    }

    private BuildResult buildAndFail() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersions')
                .withPluginClasspath()
                .buildAndFail()
    }
}
