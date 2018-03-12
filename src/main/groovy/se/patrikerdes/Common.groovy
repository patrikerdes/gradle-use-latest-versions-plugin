package se.patrikerdes

import groovy.transform.CompileStatic

@CompileStatic
class Common {
    static List<DependencyUpdate> getOutDatedDependencies(dependencyUpdatesJson) {
        def outdatedDependencies = dependencyUpdatesJson['outdated']['dependencies']
        List<DependencyUpdate> dependecyUpdates = []
        for (outdatedDependency in outdatedDependencies) {
            dependecyUpdates.add(new DependencyUpdate(outdatedDependency['group'], outdatedDependency['name'], outdatedDependency['version'], outdatedDependency['available']['milestone']))
        }
        dependecyUpdates
    }
}

