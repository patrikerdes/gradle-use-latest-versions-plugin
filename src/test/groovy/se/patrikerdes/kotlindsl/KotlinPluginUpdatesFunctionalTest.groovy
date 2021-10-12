package se.patrikerdes.kotlindsl

import se.patrikerdes.CurrentVersions
import java.util.regex.Pattern

class KotlinPluginUpdatesFunctionalTest extends KotlinBaseFunctionalTest {
    void "an outdated plugin dependency can be updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
                id("org.gradle.hello-world") version "0.1"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('id("org.gradle.hello-world") version "0.2"')
    }

    void "an outdated Kotlin plugin can be updated"() {
        given:
        buildFile << """
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "$CurrentVersions.VERSIONS"
                kotlin("jvm") version "1.4.32"
                kotlin("plugin.spring") version "1.4.32"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.eachMatch('kotlin\\(\"jvm\"\\) version \"(.+)\"') { version ->
            // saves us having to hardcode a version number which will need updating in the future
            version.size() == 2
            version[1].split('\\.')[1].toInteger() > 4
        }
        Pattern matcher = updatedBuildFile =~ 'kotlin\\(\"plugin\\.spring\"\\) version \"(.+)\"'
        matcher.size() == 1
        matcher[0][1].split('\\.')[1].toInteger() > 4
    }
}
