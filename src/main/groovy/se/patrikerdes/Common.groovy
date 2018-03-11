package se.patrikerdes

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

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

@CompileStatic
@TupleConstructor
class DependencyUpdate {
    String group
    String name
    String oldVersion
    String newVersion
    DependencyUpdate(group, name, oldVersion, newVersion) {
        this.group = group
        this.name = name
        this.oldVersion = oldVersion
        this.newVersion = newVersion
    }
    String oldModuleVersionMatchString() {
        return "([\"']" + this.group + ":" + this.name + ":)[^\$].*?([\"'])"
    }
    String newModuleVersionString() {
        return '$1' + this.newVersion + '$2'
    }
    String oldPluginVersionMatchString() {
        return "(id[ \\t]+[\"']" + this.group + "[\"'][ \\t]+version[ \\t]+[\"'])" + this.oldVersion + "([\"'])"
    }
    String newPluginVersionString() {
        return '$1' + this.newVersion + '$2'
    }
    String variableUseMatchString() {
        // Capture variables starting with $, but not expresions starting with ${
        return "[\"']" + this.group + ":" + this.name + ":[\$]([^{].*?)[\"']"
    }
}
