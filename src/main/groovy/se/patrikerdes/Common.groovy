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
                    getAvailableVersion(outdatedDependency)))
        }
        dependecyUpdates
    }

    static String getAvailableVersion(Object outdatedDependency) {
        if (outdatedDependency['available']['milestone']) {
            (String) outdatedDependency['available']['milestone']
        } else if (outdatedDependency['available']['release']) {
            (String) outdatedDependency['available']['release']
        } else if (outdatedDependency['available']['integration']) {
            (String) outdatedDependency['available']['integration']
        }
    }

    static List<DependencyUpdate> getCurrentDependencies(Object dependencyUpdatesJson) {
        Object currentDependencies = dependencyUpdatesJson['current']['dependencies']
        List<DependencyUpdate> dependencyStables = []
        for (currentDependency in currentDependencies) {
            dependencyStables.add(new DependencyUpdate((String) currentDependency['group'],
                    (String) currentDependency['name'],
                    (String) currentDependency['version'],
                    (String) currentDependency['version']))
        }
        dependencyStables
    }
}

