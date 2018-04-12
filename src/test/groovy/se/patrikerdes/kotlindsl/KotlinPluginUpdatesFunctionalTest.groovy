package se.patrikerdes.kotlindsl

import se.patrikerdes.CurrentVersions

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

}
