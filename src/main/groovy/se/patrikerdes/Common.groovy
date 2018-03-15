package se.patrikerdes

import groovy.transform.CompileStatic

@CompileStatic
class Common {
    static List<DependencyUpdate> getOutDatedDependencies(Object dependencyUpdatesJson) {
        Object outdatedDependencies = dependencyUpdatesJson['outdated']['dependencies']
        List<DependencyUpdate> dependecyUpdates = []
        for (outdatedDependency in outdatedDependencies) {
            dependecyUpdates.add(new DependencyUpdate((String)outdatedDependency['group'],
                    (String)outdatedDependency['name'],
                    (String)outdatedDependency['version'],
                    (String)outdatedDependency['available']['milestone']))
        }
        dependecyUpdates
    }
}

