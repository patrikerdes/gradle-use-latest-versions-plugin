package se.patrikerdes

class GradleVersionFunctionalTest extends BaseFunctionalTest {
    def "Gradle versions"() {
        println("Testing Gradle version $gradleVersion")
        if(System.getProperty("java.version")[0] == "9" && gradleVersion in unsupportedGradleVersionsJDK9) {
            println("Skipping this test on JDK 9, since it does not support gradle version $gradleVersion")
            return
        }

        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.versions'
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
