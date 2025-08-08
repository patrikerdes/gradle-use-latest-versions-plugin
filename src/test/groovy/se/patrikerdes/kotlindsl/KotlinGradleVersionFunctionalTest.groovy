package se.patrikerdes.kotlindsl

import se.patrikerdes.CurrentVersions

class KotlinGradleVersionFunctionalTest extends KotlinBaseFunctionalTest {
    private static final List<String> PLUGIN_UPDATE_NOT_SUPPORTED = ['2.8', '2.14', '3.0', '3.5',
                                                                     '4.0', '4.1', '4.2', '4.3']

    void "Gradle versions"() {
        println("Testing Gradle version $gradleVersion")
        if (System.getProperty('java.version')[0] == '9' && gradleVersion in GRADLE_VERSIONS_NOT_JDK9) {
            println("Skipping this test on JDK 9, since it does not support gradle version $gradleVersion")
            return
        }

        if (System.getenv('SKIP_SLOW_INTEGRATION_TESTS') != null) {
            println("Found environment variable SKIP_SLOW_INTEGRATION_TESTS, won't test gradle version $gradleVersion")
            return
        }

        if (System.getenv('TRAVIS') != null) {
            println('This test is broken, skipping (TODO: Fix)')
            return
        }

        given:
        buildFile << '''
            plugins {
                application
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "0.52.0"
            }

            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation("log4j:log4j:1.2.16")
            }
        '''

        when:
        useLatestVersions(gradleVersion)
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("log4j:log4j:$CurrentVersions.LOG4J")
        gradleVersion in PLUGIN_UPDATE_NOT_SUPPORTED ||
                updatedBuildFile.contains("id(\"com.github.ben-manes.versions\") version \"$CurrentVersions.VERSIONS\"")

        where:
        gradleVersion << [
          '8.14',
          '9.0.0',
        ]
    }
}
