package se.patrikerdes

class PluginUpdatesFunctionalTest extends BaseFunctionalTest {
    def "an outdated plugin dependency with single quotes can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
                id 'org.gradle.hello-world' version '0.1'
            }
            apply plugin: 'java'
        """

        when:
        useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("id 'org.gradle.hello-world' version '0.2'")
    }

    def "an outdated plugin dependency with double quotes can be updated"() {
        given:
        buildFile << """
            plugins {
                id "se.patrikerdes.use-latest-versions"
                id "com.github.ben-manes.versions" version "$CurrentVersions.versions"
                id "org.gradle.hello-world" version "0.1"
            }
            apply plugin: 'java'
        """

        when:
        useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('id "org.gradle.hello-world" version "0.2"')
    }

    def "an outdated plugin dependency with strange whitespace can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
                id \t'org.gradle.hello-world'\t version  \t  '0.1'
            }
            apply plugin: 'java'
        """

        when:
        useLatestVersions()
        def updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("id \t'org.gradle.hello-world'\t version  \t  '0.2'")
    }
}
