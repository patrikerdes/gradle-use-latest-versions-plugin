package se.patrikerdes

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BaseFunctionalTest extends Specification {

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    BuildResult useLatestVersions() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('useLatestVersions')
                .withPluginClasspath()
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

    BuildResult clean() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('clean')
                .withPluginClasspath()
                .build()
    }
}
