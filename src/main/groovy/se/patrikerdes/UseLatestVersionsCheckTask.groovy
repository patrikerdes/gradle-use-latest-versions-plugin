package se.patrikerdes

import static se.patrikerdes.Common.getOutDatedDependencies
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

@CompileStatic
class UseLatestVersionsCheckTask extends DefaultTask {
    @TaskAction
    void useLatestVersionsCheckTask() {
        File previousDependencyUpdatesReport =
                new File(new File(project.rootDir, 'build' + File.separator + 'useLatestVersions'),
                        'latestDependencyUpdatesReport.json')
        File currentDependencyUpdatesReport =
                new File(project.rootDir,
                        'build' + File.separator + 'dependencyUpdates' + File.separator + 'report.json')

        if (!previousDependencyUpdatesReport.exists()) {
            throw new GradleException('No results from useLatestVersions were found, aborting')
        }

        //def previousDependencyUpdatesJson = new JsonSlurper().parse(previousDependencyUpdatesReport)
        Object currentDependencyUpdatesJson = new JsonSlurper().parse(currentDependencyUpdatesReport)

        List<DependencyUpdate> leftToUpdate = getOutDatedDependencies(currentDependencyUpdatesJson)

        if (leftToUpdate.size() == 0) {
            println('useLatestVersions successfully upgraded all dependencies to their latest version')
        } else {
            throw new GradleException('useLatestVersions failed to update at least one dependency')
        }
    }
}
