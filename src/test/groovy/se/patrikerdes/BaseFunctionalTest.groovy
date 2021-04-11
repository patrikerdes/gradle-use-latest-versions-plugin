package se.patrikerdes

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BaseFunctionalTest extends Specification {

    @Rule
    protected final TemporaryFolder testProjectDir = new TemporaryFolder()
    protected File buildFile

    void setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    BuildResult useLatestVersions() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersions')
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersionsOnly(String update) {
        useLatestVersionsOnly([update])
    }

    BuildResult useLatestVersionsOnly(List<String> updates) {
        List<String> arguments = ['useLatestVersions']
        updates.each { arguments << '--update-dependency' << it }
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersionsWithout(String update) {
        useLatestVersionsWithout([update])
    }

    BuildResult useLatestVersionsWithout(List<String> updates) {
        List<String> arguments = ['useLatestVersions']
        updates.each { arguments << '--ignore-dependency' << it }
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersionsWithBlackAndWhitelist() {
        List<String> arguments = ['useLatestVersions']
        arguments << '--update-dependency' << 'junit:junit'
        arguments << '--ignore-dependency' << 'log4j:log4j'
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .buildAndFail()
    }

    BuildResult useLatestVersionsUpdatingRootProperties() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersions', '--update-root-properties')
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersionsUpdatingRootPropertiesAndFail() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersions', '--update-root-properties')
                .withPluginClasspath()
                .buildAndFail()
    }

    BuildResult useLatestVersionsWithVersionFiles(String... versionFiles) {
        List<String> arguments = ['useLatestVersions']
        for (versionFile in versionFiles) {
            arguments << '--version-files' << versionFile
        }
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersionsUpdatingRootPropertiesWithRootList(String... rootVersionsList) {
        List<String> arguments = ['useLatestVersions', '--update-root-properties']
        for (versionFile in rootVersionsList) {
            arguments << '--root-version-files' << versionFile
        }
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersions(String gradleVersion) {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersions')
                .withPluginClasspath()
                .withGradleVersion(gradleVersion)
                .build()
    }

    BuildResult useLatestVersionsAndFail() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersions')
                .withPluginClasspath()
                .buildAndFail()
    }

    BuildResult useLatestVersionsCheck() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersionsCheck')
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersionsCheckAndFail() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersionsCheck')
                .withPluginClasspath()
                .buildAndFail()
    }

    BuildResult useLatestVersionsCheckOnly(String update) {
        useLatestVersionsCheckOnly([update])
    }

    BuildResult useLatestVersionsCheckOnly(List<String> updates) {
        List<String> arguments = ['useLatestVersionsCheck']
        updates.each { arguments << '--update-dependency' << it }
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersionsCheckWithout(String update) {
        useLatestVersionsCheckWithout([update])
    }

    BuildResult useLatestVersionsCheckWithout(List<String> updates) {
        List<String> arguments = ['useLatestVersionsCheck']
        updates.each { arguments << '--ignore-dependency' << it }
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .build()
    }

    BuildResult useLatestVersionsCheckWithBlackAndWhitelist() {
        List<String> arguments = ['useLatestVersionsCheck']
        arguments << '--update-dependency' << 'junit:junit'
        arguments << '--ignore-dependency' << 'log4j:log4j'
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(arguments)
                .withPluginClasspath()
                .buildAndFail()
    }

    BuildResult clean() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('clean')
                .withPluginClasspath()
                .build()
    }

    protected static final List<String> GRADLE_VERSIONS_NOT_JDK9 = ['4.2', '4.2-rc-2', '4.2-rc-1', '4.1', '4.1-rc-2',
                                                                  '4.0.2', '4.1-rc-1', '4.0.1', '4.1-milestone-1',
                                                                  '3.5.1', '4.0', '4.0-rc-3', '4.0-rc-2',
                                                                  '4.0-rc-1', '4.0-milestone-2', '4.0-milestone-1',
                                                                  '3.5', '3.5-rc-3', '3.5-rc-2', '3.5-rc-1',
                                                                  '3.4.1', '3.4', '3.4-rc-3', '3.4-rc-2',
                                                                  '3.4-rc-1', '3.3', '3.3-rc-1', '3.2.1', '3.2',
                                                                  '3.2-rc-3', '3.2-rc-2', '3.2-rc-1', '3.1',
                                                                  '3.1-rc-1', '3.0', '3.0-rc-2', '3.0-rc-1',
                                                                  '2.14.1', '2.14.1-rc-2', '2.14.1-rc-1',
                                                                  '3.0-milestone-2', '2.14', '2.14-rc-6',
                                                                  '3.0-milestone-1', '2.14-rc-5', '2.14-rc-4',
                                                                  '2.14-rc-3', '2.14-rc-2', '2.14-rc-1', '2.13',
                                                                  '2.13-rc-2', '2.13-rc-1', '2.12', '2.12-rc-1',
                                                                  '2.11', '2.11-rc-3', '2.11-rc-2', '2.11-rc-1',
                                                                  '2.10', '2.10-rc-2', '2.10-rc-1', '2.9',
                                                                  '2.9-rc-1', '2.8', '2.8-rc-2', '2.8-rc-1',
                                                                  '2.7', '2.7-rc-2', '2.7-rc-1', '2.6', '2.6-rc-2',
                                                                  '2.6-rc-1', '2.5', '2.5-rc-2', '2.5-rc-1', '2.4',
                                                                  '2.4-rc-2', '2.4-rc-1', '2.3', '2.3-rc-4',
                                                                  '2.3-rc-3', '2.3-rc-2', '2.3-rc-1', '2.2.1',
                                                                  '2.2.1-rc-1', '2.2', '2.2-rc-2', '2.2-rc-1',
                                                                  '2.1', '2.1-rc-4', '2.1-rc-3', '2.1-rc-2',
                                                                  '2.1-rc-1', '2.0', '2.0-rc-2', '2.0-rc-1',
                                                                  '1.12', '1.12-rc-2', '1.12-rc-1', '1.11',
                                                                  '1.11-rc-1', '1.10', '1.10-rc-2', '1.10-rc-1',
                                                                  '1.9', '1.9-rc-4', '1.9-rc-3', '1.9-rc-2',
                                                                  '1.9-rc-1', '1.8', '1.8-rc-2', '1.8-rc-1',
                                                                  '1.7', '1.7-rc-2', '1.7-rc-1', '1.6', '1.6-rc-1',
                                                                  '1.5', '1.5-rc-3', '1.5-rc-2', '1.5-rc-1', '1.4',
                                                                  '1.4-rc-3', '1.4-rc-2', '1.4-rc-1', '1.3',
                                                                  '1.3-rc-2', '1.3-rc-1', '1.2', '1.2-rc-1',
                                                                  '1.1', '1.1-rc-2', '1.1-rc-1', '1.0', '1.0-rc-3',
                                                                  '1.0-rc-2', '1.0-rc-1', '1.0-milestone-9',
                                                                  '1.0-milestone-8a', '1.0-milestone-8',
                                                                  '1.0-milestone-7', '1.0-milestone-6',
                                                                  '1.0-milestone-5', '1.0-milestone-4',
                                                                  '1.0-milestone-3', '1.0-milestone-2',
                                                                  '1.0-milestone-1', '0.9.2', '0.9.1', '0.9',
                                                                  '0.9-rc-3', '0.9-rc-2', '0.9-rc-1', '0.8', '0.7']

}
