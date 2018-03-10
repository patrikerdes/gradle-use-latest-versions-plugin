package se.patrikerdes

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

@CompileStatic
class UseLatestVersionsTask extends DefaultTask {

    @TaskAction
    def useLatestVersions() {
        println 'hello from UseLatestVersionsTask'
        def dotGradleFiles = new groovy.util.FileNameFinder().getFileNames(project.rootDir.getAbsolutePath(), "**/*.gradle")
        for(dotGradleFile in dotGradleFiles) {
            def gradleFileContents = new File(dotGradleFile).getText('UTF-8')
            gradleFileContents = gradleFileContents + "\norg.assertj:assertj-core:3.9.1"
            new File(dotGradleFile).setText(gradleFileContents, 'UTF-8')
        }
    }
}
