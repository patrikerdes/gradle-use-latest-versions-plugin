package se.patrikerdes

import spock.lang.Unroll

class PluginUpdatesFunctionalTest extends BaseFunctionalTest {
    void "an outdated plugin dependency with single quotes can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
                id 'org.gradle.hello-world' version '0.1'
            }
            apply plugin: 'java'
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("id 'org.gradle.hello-world' version '0.2'")
    }

    void "an outdated plugin dependency with double quotes can be updated"() {
        given:
        buildFile << """
            plugins {
                id "se.patrikerdes.use-latest-versions"
                id "com.github.ben-manes.versions" version "$CurrentVersions.VERSIONS"
                id "org.gradle.hello-world" version "0.1"
            }
            apply plugin: 'java'
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('id "org.gradle.hello-world" version "0.2"')
    }

    void "an outdated plugin dependency with strange whitespace can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
                id \t'org.gradle.hello-world'\t version  \t  '0.1'
            }
            apply plugin: 'java'
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("id \t'org.gradle.hello-world'\t version  \t  '0.2'")
    }

    @Unroll
    void "will update an outdated plugin in root build.gradle for Multi-project #testDesc updateRootProperties"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
                id 'org.gradle.hello-world' version '0.1'
            }

            allprojects {
                apply plugin: 'se.patrikerdes.use-latest-versions'
                apply plugin: 'com.github.ben-manes.versions'
                apply plugin: 'java'
                repositories {
                    mavenCentral()
                }
            }
        """
        File rootGradleSettingsFile = testProjectDir.newFile('settings.gradle')
        rootGradleSettingsFile << '''
            include 'first-sub-project'
            include 'second-sub-project'
        '''
        File firstSubProjectFolder = testProjectDir.newFolder('first-sub-project')
        File firstSubProjectBuildFile = new File(firstSubProjectFolder, 'build.gradle')
        firstSubProjectBuildFile << '''
        '''
        File secondSubProjectFolder = testProjectDir.newFolder('second-sub-project')
        File secondSubProjectBuildFile = new File(secondSubProjectFolder, 'build.gradle')
        secondSubProjectBuildFile << '''
        '''

        when:
        "$testFunction"()

        then:
        String updatedBuildFile = buildFile.getText('UTF-8')
        updatedBuildFile.contains("id 'org.gradle.hello-world' version '0.2'")

        where:
        testFunction | testDesc
        'useLatestVersions' | 'without'
        'useLatestVersionsUpdatingRootProperties' | 'with'
    }
}
