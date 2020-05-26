package se.patrikerdes

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task

@CompileStatic
class UseLatestVersionsPlugin implements Plugin<Project> {

    static final String DEPENDENCY_UPDATES = 'dependencyUpdates'
    static final String USE_LATEST_VERSIONS = 'useLatestVersions'
    static final String USE_LATEST_VERSIONS_CHECK = 'useLatestVersionsCheck'
    static final String INTERNAL_ROOT_AGGREGATE = 'internalRootAggregate'

    void apply(Project project) {
        System.setProperty('outputFormatter', 'json,xml,plain')
        Task rootAggregate = setupRootAggregateTask(project)
        setupUseLatestVersions(project, rootAggregate)
        setupUseLatestVersionsCheck(project)
    }

    Task setupRootAggregateTask(Project project) {
        Set<Task> tasks = project.rootProject.getTasksByName(INTERNAL_ROOT_AGGREGATE, false)
        if (tasks.isEmpty()) {
            // This handles both cases: when UseLatestVersionsPlugin is applied
            // to root project and subprojects or when it is applied only to subprojects
            return project.rootProject.task(INTERNAL_ROOT_AGGREGATE, type: InternalAggregateRootTask)
        }
        tasks[0]
    }

    void setupUseLatestVersions(Project project, Task rootAggregate) {
        Task useLatestVersions = project.task(USE_LATEST_VERSIONS, type: UseLatestVersionsTask)
        useLatestVersions.dependsOn(DEPENDENCY_UPDATES)
        useLatestVersions.finalizedBy(rootAggregate)
        rootAggregate.mustRunAfter(useLatestVersions)
    }

    void setupUseLatestVersionsCheck(Project project) {
        Task useLatestVersionCheck = project.task(USE_LATEST_VERSIONS_CHECK, type: UseLatestVersionsCheckTask)
        useLatestVersionCheck.dependsOn(DEPENDENCY_UPDATES)
    }

}
