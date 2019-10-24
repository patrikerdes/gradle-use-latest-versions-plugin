package se.patrikerdes

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static se.patrikerdes.Common.WHITE_BLACKLIST_ERROR_MESSAGE

import org.gradle.testkit.runner.BuildResult

class CheckFunctionalTest extends BaseFunctionalTest {
    void "the json file of dependencyUpdates is written by the useLatestVersions task for the check task to consume"() {
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
                testCompile 'junit:junit:4.0'
            }
        """

        when:
        useLatestVersions()
        File useLatestVersionsPath = new File(new File(testProjectDir.root, 'build'), 'useLatestVersions')
        File jsonReport = new File(useLatestVersionsPath, 'latestDependencyUpdatesReport.json')

        then:
        useLatestVersionsPath.exists()
        jsonReport.exists()
        jsonReport.length() > 500  // Was 859 when the test was developed
    }

    void "the check task fails if useLatestVersions task has not run"() {
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
                testCompile 'junit:junit:4.0'
            }
        """

        when:
        BuildResult result = useLatestVersionsCheckAndFail()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains('No results from useLatestVersions were found, aborting')
    }

    void "the check task fails if clean has run between useLatestVersions and useLatestVersionsCheck"() {
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
                testCompile 'junit:junit:4.0'
            }
        """

        when:
        useLatestVersions()
        clean()
        BuildResult result = useLatestVersionsCheckAndFail()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains('No results from useLatestVersions were found, aborting')
    }

    void "useLatestVersionsCheck is successful if it runs after useLatestVersions"() {
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
                testCompile 'junit:junit:4.0'
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
    }

    void "useLatestVersionsCheck outputs success if all updates were successful"() {
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
                testCompile 'junit:junit:4.0'
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        result.output.contains('successfully updated 1 dependency')
        result.output.contains(" - junit:junit [4.0 -> $CurrentVersions.JUNIT]")
    }

    void "useLatestVersionsCheck fails if any update failed"() {
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
            
            def junit_version = '3.0'
            junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheckAndFail()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains('failed to update 1 dependency')
        result.output.contains(" - junit:junit [4.0 -> $CurrentVersions.JUNIT]")
    }

    void "useLatestVersionsCheck correctly prints one successful and one unsuccessful update"() {
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
            
            def junit_version = '3.0'
            junit_version = '4.0'
            
            dependencies {
                compile "log4j:log4j:1.2.16"
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheckAndFail()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains('useLatestVersions failed to update 1 dependency to the latest version:')
        result.output.contains("- junit:junit [4.0 -> $CurrentVersions.JUNIT]")
        result.output.contains('useLatestVersions successfully updated 1 dependency to the latest version:')
        result.output.contains("- log4j:log4j [1.2.16 -> $CurrentVersions.LOG4J]")
    }

    void "useLatestVersionsCheck notes skipped updates due to not being in --update-dependency"() {
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
                compile "log4j:log4j:1.2.16"
                testCompile "junit:junit:3.0"
            }
        """

        when:
        useLatestVersionsOnly('log4j:log4j')
        BuildResult result = useLatestVersionsCheckOnly('log4j:log4j')

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        String output = result.output.replaceAll('\r', '').replaceAll('\n', '#')
        output.contains('useLatestVersions successfully updated 1 dependency to the latest version:#' +
            " - log4j:log4j [1.2.16 -> $CurrentVersions.LOG4J]")
        output.contains('useLatestVersions skipped updating 1 dependency not in --update-dependency:#' +
            " - junit:junit [3.0 -> $CurrentVersions.JUNIT]")
    }

    void "useLatestVersionsCheck notes skipped updates due to not being in --update-dependency as group"() {
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
                compile "log4j:log4j:1.2.16"
                testCompile "junit:junit:3.0"
                testCompile "junit:junit-dep:4.9"
            }
        """

        when:
        useLatestVersionsOnly('junit')
        BuildResult result = useLatestVersionsCheckOnly('junit')

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        String output = result.output.replaceAll('\r', '').replaceAll('\n', '#')
        output.contains('useLatestVersions successfully updated 2 dependencies to the latest version:#' +
            " - junit:junit [3.0 -> $CurrentVersions.JUNIT]#" +
            ' - junit:junit-dep [4.9 -> ')
        output.contains('useLatestVersions skipped updating 1 dependency not in --update-dependency:#' +
            " - log4j:log4j [1.2.16 -> $CurrentVersions.LOG4J]")
    }

    void "useLatestVersionsCheck notes skipped updates due to being in --ignore-dependency"() {
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
                compile "log4j:log4j:1.2.16"
                testCompile "junit:junit:3.0"
            }
        """

        when:
        useLatestVersionsWithout('junit:junit')
        BuildResult result = useLatestVersionsCheckWithout('junit:junit')

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        String output = result.output.replaceAll('\r', '').replaceAll('\n', '#')
        output.contains('useLatestVersions successfully updated 1 dependency to the latest version:#' +
            " - log4j:log4j [1.2.16 -> $CurrentVersions.LOG4J]")
        output.contains('useLatestVersions skipped updating 1 dependency in --ignore-dependency:#' +
            " - junit:junit [3.0 -> $CurrentVersions.JUNIT]")
    }

    void "useLatestVersionsCheck notes skipped updates due to being in --ignore-dependency as group"() {
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
                compile "log4j:log4j:1.2.16"
                testCompile "junit:junit:3.0"
                testCompile "junit:junit-dep:4.9"
            }
        """

        when:
        useLatestVersionsWithout('junit')
        BuildResult result = useLatestVersionsCheckWithout('junit')

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        String output = result.output.replaceAll('\r', '').replaceAll('\n', '#')
        output.contains('useLatestVersions successfully updated 1 dependency to the latest version:#' +
            " - log4j:log4j [1.2.16 -> $CurrentVersions.LOG4J]")
        output.contains('useLatestVersions skipped updating 2 dependencies in --ignore-dependency:#' +
            " - junit:junit [3.0 -> $CurrentVersions.JUNIT]#" +
            ' - junit:junit-dep [4.9 -> ')
    }

    void "useLatestVersions fails if both --ignore-dependency and --update-dependency are set"() {
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
                compile "log4j:log4j:1.2.16"
                testCompile "junit:junit:3.0"
            }
        """

        when:
        BuildResult result = useLatestVersionsWithBlackAndWhitelist()

        then:
        result.task(':useLatestVersions').outcome == FAILED
        result.output.contains(WHITE_BLACKLIST_ERROR_MESSAGE)
    }

    void "useLatestVersionsCheck fails if both --ignore-dependency and --update-dependency are set"() {
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
                compile "log4j:log4j:1.2.16"
                testCompile "junit:junit:3.0"
            }
        """

        when:
        BuildResult result = useLatestVersionsCheckWithBlackAndWhitelist()

        then:
        result.task(':useLatestVersionsCheck').outcome == FAILED
        result.output.contains(WHITE_BLACKLIST_ERROR_MESSAGE)
    }

    void "useLatestVersionsCheck outputs a special message when there was nothing to update"() {
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
                testCompile 'junit:junit:$CurrentVersions.JUNIT'
            }
        """

        when:
        useLatestVersions()
        BuildResult result = useLatestVersionsCheck()

        then:
        result.task(':useLatestVersionsCheck').outcome == SUCCESS
        result.output.contains('useLatestVersions successfully did nothing')
    }
}
