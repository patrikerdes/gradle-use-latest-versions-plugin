package se.patrikerdes

import static se.patrikerdes.Common.getOutDatedDependencies
import static se.patrikerdes.Common.WHITE_BLACKLIST_ERROR_MESSAGE
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.TaskAction

@CompileStatic
class UseLatestVersionsCheckTask extends DefaultTask {
    @Option(option = 'update-dependency',
            description = 'The same argument that was passed to useLatestVersions')
    List<String> updateWhitelist = Collections.emptyList()

    @Option(option = 'ignore-dependency',
            description = 'A blacklist of dependencies to update, in the format of group:name')
    List<String> updateBlacklist = Collections.emptyList()

    UseLatestVersionsCheckTask() {
        description = 'Check if all available updates were successfully applied by the useLatestVersions task.'
        group = 'Help'
    }

    @TaskAction
    void useLatestVersionsCheckTask() {
        validateExclusiveWhiteOrBlacklist()
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

        int skippedCount = getSkippedCount(leftToUpdate)
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
            printSkippedDependenciesMessage(skippedCount, leftToUpdate)
        }

        if (updatedCount == 0 && failedCount == 0) {
            println('useLatestVersions successfully did nothing; there was nothing to update')
        }

        if (failedCount > 0) {
            throw new GradleException('useLatestVersions failed')
        }
    }

    private void printSkippedDependenciesMessage(int skippedCount, List<DependencyUpdate> leftToUpdate) {
        if (!updateWhitelist.empty) {
            println("useLatestVersions skipped updating $skippedCount ${deps(skippedCount)} " +
                    'not in --update-dependency:')
            for (dependencyLeftToUpdate in leftToUpdate) {
                if (!updateWhitelist.contains(dependencyLeftToUpdate.groupAndName())) {
                    println(' - ' + dependencyLeftToUpdate)
                }
            }
        }
        if (!updateBlacklist.empty) {
            println("useLatestVersions skipped updating $skippedCount ${deps(skippedCount)} " +
                    'in --ignore-dependency:')
            for (dependencyLeftToUpdate in leftToUpdate) {
                if (updateBlacklist.contains(dependencyLeftToUpdate.groupAndName())) {
                    println(' - ' + dependencyLeftToUpdate)
                }
            }
        }
    }

    private int getSkippedCount(List<DependencyUpdate> leftToUpdate) {
        int skippedCount = updateWhitelist.empty ? 0 :
                leftToUpdate.count { !updateWhitelist.contains(it.groupAndName()) } as int
        if (!updateBlacklist.empty) {
            skippedCount = leftToUpdate.count { updateBlacklist.contains(it.groupAndName()) } as int
        }
        skippedCount
    }

    private void validateExclusiveWhiteOrBlacklist() {
        if (!updateWhitelist.empty && !updateBlacklist.empty) {
            throw new GradleException(WHITE_BLACKLIST_ERROR_MESSAGE)
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
