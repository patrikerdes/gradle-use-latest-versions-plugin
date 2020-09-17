package se.patrikerdes.VariableUpdatesTypeSpecificFunctionalTests

import org.gradle.testkit.runner.BuildResult
import se.patrikerdes.BaseFunctionalTest
import se.patrikerdes.CurrentVersions

class VariableUpdatesGradlePropertiesFunctionalTest extends BaseFunctionalTest {
    void "a variable assigned in gradle.properties will be updated"() {
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
                testCompile "junit:junit:\$junit_version"
                compile "log4j:log4j:\$log4j_version"
            }
        """
        File gradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        gradlePropertiesFile << '''
            junit_version = 4.0
            log4j_version=1.2.16
        '''

        when:
        useLatestVersions()
        String updatedGradlePropertiesFile = gradlePropertiesFile.getText('UTF-8')

        then:
        updatedGradlePropertiesFile.contains("junit_version = $CurrentVersions.JUNIT")
        updatedGradlePropertiesFile.contains("log4j_version=$CurrentVersions.LOG4J")
    }

    void "will update variables in gradle.properties when specified twice in build gradle"() {
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
                testCompile "junit:junit:\$junit_version"
                testCompile "log4j:log4j:\$log4j_version"
                runtimeOnly "log4j:log4j:\$log4j_version"
            }
        """
        File gradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        gradlePropertiesFile << '''
            junit_version = 4.0
            log4j_version=1.2.16
        '''

        when:
        useLatestVersions()
        String updatedGradlePropertiesFile = gradlePropertiesFile.getText('UTF-8')

        then:
        updatedGradlePropertiesFile.contains("junit_version = $CurrentVersions.JUNIT")
        updatedGradlePropertiesFile.contains("log4j_version=$CurrentVersions.LOG4J")
    }

    void "will not update variables in root gradle.properties in Multi-project without --update-root-properties"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
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
        File rootGradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        rootGradlePropertiesFile << '''
            junit_version = 4.0
            log4j_version=1.2.16
        '''
        File rootGradleSettingsFile = testProjectDir.newFile('settings.gradle')
        rootGradleSettingsFile << '''
            include 'sub-project'
        '''
        File subProjectFolder = testProjectDir.newFolder('sub-project')
        File subProjectBuildFile = new File(subProjectFolder, 'build.gradle')
        subProjectBuildFile << '''
            dependencies {
                testCompile "junit:junit:\$junit_version"
                compile "log4j:log4j:\$log4j_version"
            }
        '''

        when:
        useLatestVersions()

        then:
        String updatedGradlePropertiesFile = rootGradlePropertiesFile.getText('UTF-8')
        updatedGradlePropertiesFile.contains('junit_version = 4.0')
        updatedGradlePropertiesFile.contains('log4j_version=1.2.16')
    }

    void "will update variables in root gradle.properties for Multi-project build"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
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
        File rootGradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        rootGradlePropertiesFile << '''
            junit_version = 4.0
            log4j_version=1.2.16
        '''
        File rootGradleSettingsFile = testProjectDir.newFile('settings.gradle')
        rootGradleSettingsFile << '''
            include 'sub-project'
        '''
        File subProjectFolder = testProjectDir.newFolder('sub-project')
        File subProjectBuildFile = new File(subProjectFolder, 'build.gradle')
        subProjectBuildFile << '''
            dependencies {
                testCompile "junit:junit:\$junit_version"
                compile "log4j:log4j:\$log4j_version"
            }
        '''

        when:
        useLatestVersionsUpdatingRootProperties()

        then:
        String updatedGradlePropertiesFile = rootGradlePropertiesFile.getText('UTF-8')
        updatedGradlePropertiesFile.contains("junit_version = $CurrentVersions.JUNIT")
        updatedGradlePropertiesFile.contains("log4j_version=$CurrentVersions.LOG4J")
    }

    void "will update variables in root gradle.properties for Multi-project when plugin applied to subproject only"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions' apply false
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS' apply false
            }

            subprojects {
                apply plugin: 'se.patrikerdes.use-latest-versions'
                apply plugin: 'com.github.ben-manes.versions'
                apply plugin: 'java'
                repositories {
                    mavenCentral()
                }
            }
        """
        File rootGradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        rootGradlePropertiesFile << '''
            junit_version = 4.0
            log4j_version=1.2.16
        '''
        File rootGradleSettingsFile = testProjectDir.newFile('settings.gradle')
        rootGradleSettingsFile << '''
            include 'sub-project'
        '''
        File subProjectFolder = testProjectDir.newFolder('sub-project')
        File subProjectBuildFile = new File(subProjectFolder, 'build.gradle')
        subProjectBuildFile << '''
            dependencies {
                testCompile "junit:junit:\$junit_version"
                compile "log4j:log4j:\$log4j_version"
            }
        '''

        when:
        useLatestVersionsUpdatingRootProperties()

        then:
        String updatedGradlePropertiesFile = rootGradlePropertiesFile.getText('UTF-8')
        updatedGradlePropertiesFile.contains("junit_version = $CurrentVersions.JUNIT")
        updatedGradlePropertiesFile.contains("log4j_version=$CurrentVersions.LOG4J")
    }

    void "will update variables in root gradle.properties for Multi-project when present in multiple projects"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
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
        File rootGradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        rootGradlePropertiesFile << '''
            junit_version = 4.0
            log4j_version=1.2.16
        '''
        File rootGradleSettingsFile = testProjectDir.newFile('settings.gradle')
        rootGradleSettingsFile << '''
            include 'first-sub-project'
            include 'second-sub-project'
        '''
        File firstSubProjectFolder = testProjectDir.newFolder('first-sub-project')
        File firstSubProjectBuildFile = new File(firstSubProjectFolder, 'build.gradle')
        firstSubProjectBuildFile << '''
            dependencies {
                testCompile "junit:junit:\$junit_version"
                compile "log4j:log4j:\$log4j_version"
            }
        '''
        File secondSubProjectFolder = testProjectDir.newFolder('second-sub-project')
        File secondSubProjectBuildFile = new File(secondSubProjectFolder, 'build.gradle')
        secondSubProjectBuildFile << '''
            dependencies {
                testCompile "junit:junit:\$junit_version"
                compile "log4j:log4j:\$log4j_version"
            }
        '''

        when:
        useLatestVersionsUpdatingRootProperties()

        then:
        String updatedGradlePropertiesFile = rootGradlePropertiesFile.getText('UTF-8')
        updatedGradlePropertiesFile.contains("junit_version = $CurrentVersions.JUNIT")
        updatedGradlePropertiesFile.contains("log4j_version=$CurrentVersions.LOG4J")
    }

    void "will not update variables in root gradle.properties for Multi-project when not resolved to same version"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions' apply false
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS' apply false
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
        File rootGradlePropertiesFile = testProjectDir.newFile('gradle.properties')
        rootGradlePropertiesFile << '''
            junit_version = 4.0
            log4j_version=1.2.16
        '''
        File rootGradleSettingsFile = testProjectDir.newFile('settings.gradle')
        rootGradleSettingsFile << '''
            include 'first-sub-project'
            include 'second-sub-project'
        '''
        File firstSubProjectFolder = testProjectDir.newFolder('first-sub-project')
        File firstSubProjectBuildFile = new File(firstSubProjectFolder, 'build.gradle')
        firstSubProjectBuildFile << '''
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        '''
        File secondSubProjectFolder = testProjectDir.newFolder('second-sub-project')
        File secondSubProjectBuildFile = new File(secondSubProjectFolder, 'build.gradle')
        secondSubProjectBuildFile << '''
            dependencies {
                testCompile "junit:junit-dep:\$junit_version"
            }
        '''

        when:
        BuildResult result = useLatestVersionsUpdatingRootProperties()

        then:
        String updatedGradlePropertiesFile = rootGradlePropertiesFile.getText('UTF-8')
        updatedGradlePropertiesFile.contains('junit_version = 4.0')
        result.output.contains("A problem was detected: the variable 'junit_version' has different updated versions " +
                "in different projects.\nNew updated versions are: '$CurrentVersions.JUNIT' and " +
                "'$CurrentVersions.JUNIT_DEPS', root config file value won't be be changed.")
    }

}
