package se.patrikerdes.kotlindsl

import se.patrikerdes.BaseFunctionalTest
import se.patrikerdes.CurrentVersions

class KotlinGradleVersionFunctionalTest extends BaseFunctionalTest {
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

        given:
        buildFile << '''
            plugins {
                application
                kotlin("jvm")
                java
                id("se.patrikerdes.use-latest-versions")
                id("com.github.ben-manes.versions") version "0.16.0"
            }

            apply {
                plugin("org.junit.platform.gradle.plugin")
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testCompile("junit:junit:4.0")
            }
        '''

        when:
        useLatestVersions(gradleVersion)
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.JUNIT")
        gradleVersion in PLUGIN_UPDATE_NOT_SUPPORTED ||
                updatedBuildFile.contains("'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'")

        where:
        gradleVersion << [
                '2.8',
                '2.14',
                '3.0',
                '3.5',
                '4.0',
                '4.1',
                '4.2',
                '4.3',
                '4.4',
                '4.5',
                '4.6',
        ]
    }
}
