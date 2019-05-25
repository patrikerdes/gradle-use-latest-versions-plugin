package se.patrikerdes

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import org.gradle.testkit.runner.BuildResult

class BuildFileSettingsFunctionalTest extends BaseFunctionalTest {
    void "non-standard buildDir setting, update task"() {
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

            buildDir = file 'gradle-build'
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.JUNIT")
    }

    void "non-standard buildDir setting, check task"() {
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

            buildDir = file 'gradle-build'
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
    }
}
