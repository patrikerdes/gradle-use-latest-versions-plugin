package se.patrikerdes

import org.gradle.testkit.runner.BuildResult

class VariableUpdatesFunctionalTest extends BaseFunctionalTest {
    void "an outdated module dependency based on a variable can be updated, single quotes"() {
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
            
            def junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
    }

    void "an outdated module dependency based on a variable can be updated, double quotes"() {
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
            
            def junit_version = "4.0"
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = \"$CurrentVersions.JUNIT\"")
    }

    void "an outdated module dependency based on a variable can be updated, no spacing around the ="() {
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
            
            def junit_version='4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version='$CurrentVersions.JUNIT'")
    }

    void "an outdated module dependency based on a variable can be updated, tabs around the ="() {
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
            
            def junit_version\t=\t'4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version\t=\t'$CurrentVersions.JUNIT'")
    }

    void "an outdated module dependency based on a variable can be updated, plus"() {
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
            
            def junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:"+ junit_version
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
    }

    void "an outdated map notation module dependency based on a variable can be updated"() {
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
            
            def junit_version = "4.0"
            
            dependencies {
                testCompile group: 'junit', name: 'junit', version: junit_version
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = \"$CurrentVersions.JUNIT\"")
    }

    void "Extra properties extensions can be updated"() {
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
            
            ext.junit_version = '4.0'
            
            dependencies {
                testCompile group: 'junit', name: 'junit', version: junit_version
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("ext.junit_version = '$CurrentVersions.JUNIT'")
    }

    void "an expression with \${x} will be updated"() {
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
            
            def junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\${junit_version}"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
    }

    void "a variable used for multiple dependencies with different latest versions won't be updated"() {
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
            
            def commons = "1.0"
            
            dependencies {
                compile "commons-lang:commons-lang:\$commons"
                compile "commons-logging:commons-logging:\$commons"
            }
        """

        when:
        BuildResult result = useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('commons = "1.0"')
        result.output.contains('A problem was detected')
    }

    void "a variable for deps with different latest versions where one dependency is current won't be updated"() {
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
            
            def commons = "1.2"
            
            dependencies {
                compile "commons-codec:commons-codec:\$commons"       // Latest version: 1.2
                compile "commons-logging:commons-logging:\$commons"   // Latest version: 1.11
            }
        """

        when:
        BuildResult result = useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('commons = "1.2"')
        result.output.contains('A problem was detected')
    }

    void "a variable defined more than once won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            def junit_version = '3.7'

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        BuildResult result = useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        !updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
        result.output.contains('A problem was detected')
    }

    void "a variable assigned to in more than one file won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            project.ext.junit_version = '3.7'

            apply from: 'second.gradle'

            apply plugin: 'java'

            repositories {
                mavenCentral()
            }

            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """
        File secondFile = testProjectDir.newFile('second.gradle')
        secondFile << """
            rootProject.junit_version = '4.0'
        """

        when:
        BuildResult result = useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')
        String updatedSecondFile = secondFile.getText('UTF-8')

        then:
        !updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
        !updatedSecondFile.contains("junit_version = '$CurrentVersions.JUNIT'")
        result.output.contains('A problem was detected')
    }

    void "spring gradle dependency management plugin annotation"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
                id "io.spring.dependency-management" version "1.0.6.RELEASE"
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            def junit_version = '4.0'
            def log4j_version = '1.2.16'
            
            dependencyManagement {
                dependencies {
                    dependency "junit:junit:\$junit_version"
                    dependencySet(group: 'log4j', version: log4j_version) {
                        entry 'log4j'
                    }
                }
            }
            
            dependencies {
                testCompile "junit:junit"
                compile "log4j:log4j"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("def junit_version = '$CurrentVersions.JUNIT'")
        updatedBuildFile.contains("def log4j_version = '$CurrentVersions.LOG4J'")
        updatedBuildFile.contains("""
            dependencyManagement {
                dependencies {
                    dependency "junit:junit:\$junit_version"
                    dependencySet(group: 'log4j', version: log4j_version) {
                        entry 'log4j'
                    }
                }
            }""")
    }

    void "a variable assigned in gradle properties will be updated"() {
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

    void "will update variables in gradle properties when specified twice in build gradle"() {
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
                "'$CurrentVersions.JUNIT_DEPS', root gradle.properties value won't be be changed.")
    }

}
