package se.patrikerdes

class ModuleUpdatesFunctionalTest extends BaseFunctionalTest {
    void "an outdated module dependency with a fixed version can be updated, string notation"() {
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
                testImplementation 'junit:junit:4.0'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.JUNIT")
    }

    void "string notation with classifier and extension"() {
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
                testImplementation "junit:junit:4.0:javadoc@jar"
                implementation "log4j:log4j:1.2.16"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("testImplementation \"junit:junit:$CurrentVersions.JUNIT:javadoc@jar\"")
        updatedBuildFile.contains("implementation \"log4j:log4j:$CurrentVersions.LOG4J\"")
    }

    void "classifier bug (issue #21)"() {
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
            
            ext.log4jVersion = '1.2.16'
            
            dependencies {
                implementation "log4j:log4j:" + log4jVersion
                implementation group: 'junit', name: 'junit', version: '4.0'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("ext.log4jVersion = '$CurrentVersions.LOG4J'")
        updatedBuildFile.contains('implementation "log4j:log4j:" + log4jVersion')
        updatedBuildFile.contains("implementation group: 'junit', name: 'junit', version: '$CurrentVersions.JUNIT'")
    }

    void "one outdated and one up-to-date module dependency with a fixed version can be updated, string notation"() {
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
                implementation "log4j:log4j:1.2.16:javadoc"
                testImplementation "junit:junit:$CurrentVersions.JUNIT:javadoc"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("implementation \"log4j:log4j:$CurrentVersions.LOG4J:javadoc\"")
        updatedBuildFile.contains("testImplementation \"junit:junit:$CurrentVersions.JUNIT:javadoc\"")
    }

    void "an outdated dependency with a fixed version can be updated, map notation with classifier and extension"() {
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
                testImplementation group: 'junit', name: 'junit', version: '4.0', classifier: 'javadoc', ext: 'jar'
                implementation "log4j:log4j:1.2.16"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("testImplementation group: 'junit', name: 'junit', " +
          "version: '$CurrentVersions.JUNIT', " + "classifier: 'javadoc', ext: 'jar'")
        updatedBuildFile.contains("implementation \"log4j:log4j:$CurrentVersions.LOG4J\"")
    }

    void "an outdated module dependency with a fixed version can be updated, map notation, single quotes"() {
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
                testImplementation group: 'junit', name: 'junit', version: '4.0'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("group: 'junit', name: 'junit', version: '$CurrentVersions.JUNIT'")
    }

    void "an outdated module dependency with a fixed version can be updated, map notation, double quotes"() {
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
                testImplementation group: "junit", name: "junit", version: "4.0"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("group: \"junit\", name: \"junit\", version: \"$CurrentVersions.JUNIT\"")
    }

    void "an outdated module dependency with a version range can be updated"() {
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
                testImplementation 'junit:junit:[3.0,4.0]'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.JUNIT")
    }

    void "a module dependency without a fixed version which is up to date is not updated"() {
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
                testImplementation 'junit:junit:4+'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('junit:junit:4+')
    }

    void "outdated module dependencies in multiple build files can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
            
            apply plugin: 'java'
            
            apply from: 'second.gradle'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation 'junit:junit:4.0'
            }
        """
        File secondFile = testProjectDir.newFile('second.gradle')
        secondFile << """
            dependencies {
                implementation 'log4j:log4j:1.2.16'
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')
        String updatedSecondFile = secondFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit:junit:$CurrentVersions.JUNIT")
        updatedSecondFile.contains("log4j:log4j:$CurrentVersions.LOG4J")
    }

    void "spring gradle dependency management plugin annotation with variable"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
                id "io.spring.dependency-management" version "1.1.7"
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencyManagement {
                dependencies {
                    dependency "junit:junit:4.0"
                    dependencySet(group: 'log4j', version: "1.2.16") {
                        entry 'log4j'
                    }
                }
            }
            
            dependencies {
                testImplementation "junit:junit"
                implementation "log4j:log4j"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("dependency \"junit:junit:$CurrentVersions.JUNIT\"")
        updatedBuildFile.contains("dependencySet(group: 'log4j', version: \"$CurrentVersions.LOG4J\")")
    }

    void "only updates whitelisted dependencies with --update-dependency"() {
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
                testImplementation "junit:junit:4.0:javadoc@jar"
                implementation "log4j:log4j:1.2.16"
            }
        """

        when:
        useLatestVersionsOnly('log4j:log4j')
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('testImplementation \"junit:junit:4.0:javadoc@jar\"')
        updatedBuildFile.contains("implementation \"log4j:log4j:$CurrentVersions.LOG4J\"")
    }

    void "only updates whitelisted dependencies with --update-dependency as group"() {
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
                testImplementation "junit:junit:4.0:javadoc@jar"
                implementation "log4j:log4j:1.2.16"
            }
        """

        when:
        useLatestVersionsOnly('log4j')
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('testImplementation \"junit:junit:4.0:javadoc@jar\"')
        updatedBuildFile.contains("implementation \"log4j:log4j:$CurrentVersions.LOG4J\"")
    }

    void "don't updates blacklisted dependencies with --ignore-dependency"() {
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
                testImplementation "junit:junit:4.0:javadoc@jar"
                implementation "log4j:log4j:1.2.16"
            }
        """

        when:
        useLatestVersionsWithout('junit:junit')
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('testImplementation \"junit:junit:4.0:javadoc@jar\"')
        updatedBuildFile.contains("implementation \"log4j:log4j:$CurrentVersions.LOG4J\"")
    }

    void "don't updates blacklisted dependencies with --ignore-dependency as group"() {
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
                testImplementation "junit:junit:4.0:javadoc@jar"
                testImplementation "junit:junit-dep:4.9"
                implementation "log4j:log4j:1.2.16"
            }
        """

        when:
        useLatestVersionsWithout('junit')
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('testImplementation \"junit:junit:4.0:javadoc@jar\"')
        updatedBuildFile.contains('testImplementation "junit:junit-dep:4.9"')
        updatedBuildFile.contains("implementation \"log4j:log4j:$CurrentVersions.LOG4J\"")
    }
}
