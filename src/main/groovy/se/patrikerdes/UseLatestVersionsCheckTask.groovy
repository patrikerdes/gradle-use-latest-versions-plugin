package se.patrikerdes

import static se.patrikerdes.Common.getOutDatedDependencies
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.TaskAction

@CompileStatic
class UseLatestVersionsCheckTask extends DefaultTask {
    @Option(option='update-dependency',
            description = 'The same argument that was passed to useLatestVersions')
    List<String> updateWhitelist = Collections.emptyList()

    UseLatestVersionsCheckTask() {
        description = 'Check if all available updates were successfully applied by the useLatestVersions task.'
        group = 'Help'
    }

    @TaskAction
    void useLatestVersionsCheckTask() {
        File previousDependencyUpdatesReport =
                new File(new File(project.buildDir, 'useLatestVersions'), 'latestDependencyUpdatesReport.json')
        File currentDependencyUpdatesReport =
                new File(project.buildDir, 'dependencyUpdates' + File.separator + 'report.json')

        if (!previousDependencyUpdatesReport.exists()) {
            throw new GradleException('No results from useLatestVersions were found, aborting')
        }

        Object previousDependencyUpdatesJson = new JsonSlurper().parse(previousDependencyUpdatesReport)
        Object currentDependencyUpdatesJson = new JsonSlurper().parse(currentDependencyUpdatesReport)

        List<DependencyUpdate> wasUpdateable = getOutDatedDependencies(previousDependencyUpdatesJson)
        List<DependencyUpdate> leftToUpdate = getOutDatedDependencies(currentDependencyUpdatesJson)

        int skippedCount = updateWhitelist.empty ? 0 :
                leftToUpdate.count { !updateWhitelist.contains(it.groupAndName()) } as int
        int failedCount = leftToUpdate.size() - skippedCount
        if (failedCount > 0) {
            println("useLatestVersions failed to update $failedCount ${deps(failedCount)} " +
                    'to the latest version:')
            for (dependencyUpdate in leftToUpdate) {
                if (skippedCount == 0 || updateWhitelist.contains(dependencyUpdate.groupAndName())) {
                    println(' - ' + dependencyUpdate)
                }
            }
        }

        int updatedCount = wasUpdateable.size() - leftToUpdate.size()
        if (updatedCount < 0) {
            updatedCount = 0
        }

        if (updatedCount > 0) {
            println("useLatestVersions successfully updated $updatedCount ${deps(updatedCount)} " +
                    'to the latest version:')
            for (dependencyWasUpdateable in wasUpdateable) {
                boolean wasUpdated = true
                for (dependencyLeftToUpdate in leftToUpdate) {
                    if (dependencyWasUpdateable.groupAndName() == dependencyLeftToUpdate.groupAndName()) {
                        wasUpdated = false
                    }
                }
                if (wasUpdated) {
                    println(' - ' + dependencyWasUpdateable)
                }
            }
        }

        if (skippedCount > 0) {
            println("useLatestVersions skipped updating $skippedCount ${deps(skippedCount)} " +
                    'not in --dependency-update:')
            for (dependencyLeftToUpdate in leftToUpdate) {
                if (!updateWhitelist.contains(dependencyLeftToUpdate.groupAndName())) {
                    println(' - ' + dependencyLeftToUpdate)
                }
            }
        }

        if (updatedCount == 0 && failedCount == 0) {
            println('useLatestVersions successfully did nothing; there was nothing to update')
        }

        if (failedCount > 0) {
            throw new GradleException('useLatestVersions failed')
        }
    }

    static String deps(int i) {
        if (i == 1) {
            'dependency'
        } else {
            'dependencies'
        }
    }
}
