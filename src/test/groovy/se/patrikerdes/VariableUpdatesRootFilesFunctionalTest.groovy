package se.patrikerdes

import static org.gradle.testkit.runner.TaskOutcome.FAILED

import org.gradle.testkit.runner.BuildResult

import spock.lang.Unroll

class VariableUpdatesRootFilesFunctionalTest extends BaseFunctionalTest {
    @Unroll
    void "will update module versions in build.gradle with root dependencies #testDesc --update-root-properties"() {
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
    
                dependencies {
                    compile "log4j:log4j:1.2.16"
                }
            """
        File rootGradleSettingsFile = testProjectDir.newFile('settings.gradle')
        rootGradleSettingsFile << '''
        include 'sub-project'
        '''
        File subProjectFolder = testProjectDir.newFolder('sub-project')
        File subProjectBuildFile = new File(subProjectFolder, 'build.gradle')
        subProjectBuildFile << '''
        '''

        when:
        "${testFunction}"()

        then:
        String updatedBuildFile = buildFile.getText('UTF-8')
        updatedBuildFile.contains("log4j:log4j:$CurrentVersions.LOG4J")

        where:
        testFunction | testDesc
        'useLatestVersions' | 'without'
        'useLatestVersionsUpdatingRootProperties' | 'with'
    }

    @Unroll
    void "will NOT update root build.gradle without root dependencies #testDesc --update-root-properties"() {
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
    
                ext {
                    junit_version = '4.0'
                }
            """
        File rootGradleSettingsFile = testProjectDir.newFile('settings.gradle')
        rootGradleSettingsFile << '''
        include 'sub-project'
        '''
        File subProjectFolder = testProjectDir.newFolder('sub-project')
        File subProjectBuildFile = new File(subProjectFolder, 'build.gradle')
        subProjectBuildFile << '''
        dependencies {
            testCompile "junit:junit:\$junit_version"
        }
        '''

        when:
        "${testFunction}"()

        then:
        String updatedBuildFile = buildFile.getText('UTF-8')
        updatedBuildFile.contains("junit_version = '4.0'")

        where:
        testFunction | testDesc
        'useLatestVersions' | 'without'
        'useLatestVersionsUpdatingRootProperties' | 'with'
    }

    void "will not update variables in root gradle properties in Multi-project without --update-root-properties"() {
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

    void "will update variables in root gradle properties for Multi-project build"() {
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

    void "will update variables in root gradle properties for Multi-project when plugin applied to subproject only"() {
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

    void "will update variables in root gradle properties for Multi-project when present in multiple projects"() {
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

    @Unroll
    void "will #notUpdate update variables in root config with subproject deps #testDesc --update-root-properties"() {
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
                    apply from: "\$rootDir/versions.gradle"
                    repositories {
                        mavenCentral()
                    }
    
                    useLatestVersions {
                        rootVersionFiles = ['versions.gradle']
                    }
                }
            """
        File versionsFile = testProjectDir.newFile('versions.gradle')
        versionsFile << '''
        ext {
            junit_version = '4.0'
            log4j_version="1.2.16"
        }
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
            compile "log4j:log4j:\$log4j_version"
        }
        '''
        File secondSubProjectFolder = testProjectDir.newFolder('second-sub-project')
        File secondSubProjectBuildFile = new File(secondSubProjectFolder, 'build.gradle')
        secondSubProjectBuildFile << '''
        dependencies {
            testCompile "junit:junit:\$junit_version"
        }
        '''

        when:
        "$testFunction"()

        then:
        String updatedVersionsFile = versionsFile.getText('UTF-8')
        updatedVersionsFile.contains("junit_version = '$expectedJUnit'")
        updatedVersionsFile.contains("log4j_version=\"$expectedLog4J\"")

        where:
        testFunction | testDesc | notUpdate | expectedJUnit | expectedLog4J
        'useLatestVersions' | 'without' | 'NOT' | '4.0' | '1.2.16'
        'useLatestVersionsUpdatingRootProperties' | 'with' | '' | CurrentVersions.JUNIT | CurrentVersions.LOG4J
    }

    void "will update multiple custom root configs with deps from multiple projects with --update-root-properties"() {
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
                    apply from: "\$rootDir/versions.gradle"
                    apply from: "\$rootDir/otherVersions.gradle"
                    repositories {
                        mavenCentral()
                    }
    
                    useLatestVersions {
                        rootVersionFiles = ['versions.gradle', 'otherVersions.gradle']
                    }
                }
            """
        File versionsFile = testProjectDir.newFile('versions.gradle')
        versionsFile << '''
        ext {
            junit_version = '4.0'
        }
        '''
        File otherVersionsFile = testProjectDir.newFile('otherVersions.gradle')
        otherVersionsFile << '''
        ext {
            log4j_version="1.2.16"
        }
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
            compile "log4j:log4j:\$log4j_version"
        }
        '''
        File secondSubProjectFolder = testProjectDir.newFolder('second-sub-project')
        File secondSubProjectBuildFile = new File(secondSubProjectFolder, 'build.gradle')
        secondSubProjectBuildFile << '''
        dependencies {
            testCompile "junit:junit:\$junit_version"
        }
        '''

        when:
        useLatestVersionsUpdatingRootProperties()

        then:
        String updatedVersionsFile = versionsFile.getText('UTF-8')
        updatedVersionsFile.contains("junit_version = '$CurrentVersions.JUNIT'")
        String updatedOtherVersionsFile = otherVersionsFile.getText('UTF-8')
        updatedOtherVersionsFile.contains("log4j_version=\"$CurrentVersions.LOG4J\"")
    }

    void "will update multiple custom root configs with --update-root-properties and --root-version-files"() {
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
                    apply from: "\$rootDir/versions.gradle"
                    apply from: "\$rootDir/otherVersions.gradle"
                    repositories {
                        mavenCentral()
                    }
                }
            """
        File versionsFile = testProjectDir.newFile('versions.gradle')
        versionsFile << '''
        ext {
            junit_version = '4.0'
        }
        '''
        File otherVersionsFile = testProjectDir.newFile('otherVersions.gradle')
        otherVersionsFile << '''
        ext {
            log4j_version="1.2.16"
        }
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
            compile "log4j:log4j:\$log4j_version"
        }
        '''
        File secondSubProjectFolder = testProjectDir.newFolder('second-sub-project')
        File secondSubProjectBuildFile = new File(secondSubProjectFolder, 'build.gradle')
        secondSubProjectBuildFile << '''
        dependencies {
            testCompile "junit:junit:\$junit_version"
        }
        '''

        when:
        useLatestVersionsUpdatingRootPropertiesWithRootList('versions.gradle', 'otherVersions.gradle')

        then:
        String updatedVersionsFile = versionsFile.getText('UTF-8')
        updatedVersionsFile.contains("junit_version = '$CurrentVersions.JUNIT'")
        String updatedOtherVersionsFile = otherVersionsFile.getText('UTF-8')
        updatedOtherVersionsFile.contains("log4j_version=\"$CurrentVersions.LOG4J\"")
    }

    void "will not update variables in root gradle properties for Multi-project when not resolved to same version"() {
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

    void "will not update variables in root custom config for Multi-project when not resolved to same version"() {
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
                    apply from: "\$rootDir/versions.gradle"
                    repositories {
                        mavenCentral()
                    }
    
                    useLatestVersions {
                        rootVersionFiles = ['versions.gradle']
                    }
                }
            """
        File versionsFile = testProjectDir.newFile('versions.gradle')
        versionsFile << '''
        ext {
            junit_version = '4.0'
        }
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
        String updatedVersionsFile = versionsFile.getText('UTF-8')
        updatedVersionsFile.contains('junit_version = \'4.0\'')
        result.output.contains("A problem was detected: the variable 'junit_version' has different updated versions " +
                "in different projects.\nNew updated versions are: '$CurrentVersions.JUNIT' and " +
                "'$CurrentVersions.JUNIT_DEPS', root config file value won't be be changed.")
    }

    void "will error with duplicate variables in multiple root custom configs with --update-root-properties"() {
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
                    apply from: "\$rootDir/versions.gradle"
                    apply from: "\$rootDir/otherVersions.gradle"
                    repositories {
                        mavenCentral()
                    }
    
                    useLatestVersions {
                        rootVersionFiles = ['versions.gradle', 'otherVersions.gradle']
                    }
                }
            """
        File versionsFile = testProjectDir.newFile('versions.gradle')
        versionsFile << '''
        ext {
            junit_version = '4.0'
        }
        '''
        File otherVersionsFile = testProjectDir.newFile('otherVersions.gradle')
        otherVersionsFile << '''
        ext {
            junit_version = '4.0'
        }
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
            testCompile "junit:junit:\$junit_version"
        }
        '''

        when:
        BuildResult result = useLatestVersionsUpdatingRootProperties()

        then:
        String updatedVersionsFile = versionsFile.getText('UTF-8')
        updatedVersionsFile.contains("junit_version = '4.0'")
        String updatedOtherVersionsFile = otherVersionsFile.getText('UTF-8')
        updatedOtherVersionsFile.contains("junit_version = '4.0'")
        result.output.contains('A problem was detected: the variable junit_version is assigned more than ' +
                'once and won\'t be changed')
    }

    void "will generate an error if build.gradle is specified as a rootVersionFile with --update-root-properties"() {
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
    
                    useLatestVersions {
                        rootVersionFiles = ['version.gradle', 'build.gradle', 'otherVersions.gradle']
                    }
                }
            """

        when:
        BuildResult result = useLatestVersionsUpdatingRootPropertiesAndFail()

        then:
        result.task(':useLatestVersions').outcome == FAILED
        result.output.contains('The version file list contains build.gradle which is not allowed.')
    }
}
