package se.patrikerdes

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task

import java.util.regex.Matcher

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

    static void getVariablesFromMatches(Matcher variableMatch, Map<String, String> versionVariables,
                                        DependencyUpdate update, Set problemVariables) {
        if (variableMatch.size() == 1) {
            String variableName = ((List) variableMatch[0])[1]
            if (versionVariables.containsKey(variableName) &&
                    versionVariables[variableName as String] != update.newVersion) {
                println("A problem was detected: $variableName is supposed to be updated to both " +
                        "${versionVariables[variableName as String]} and ${update.newVersion}, it won't be changed.")
                problemVariables.add(variableName)
            } else {
                versionVariables.put(variableName as String, update.newVersion)
            }
        }
    }

    static Map<String, String> findVariables(List<String> dotGradleFileNames,
                                             List<DependencyUpdate> dependencyUpdates,
                                             Map<String, String> gradleFileContents,
                                             Set problemVariables) {
        Map<String, String> versionVariables = [:]
        for (String dotGradleFileName in dotGradleFileNames) {
            for (DependencyUpdate update in dependencyUpdates) {
                // Variable in string format with string interpolation
                Matcher variableMatch = gradleFileContents[dotGradleFileName] =~
                        update.variableUseStringFormatInterpolationMatchString()
                getVariablesFromMatches(variableMatch, versionVariables, update, problemVariables)

                // Variable in string format with plus
                variableMatch = gradleFileContents[dotGradleFileName] =~
                        update.variableUseStringFormatPlusMatchString()
                getVariablesFromMatches(variableMatch, versionVariables, update, problemVariables)

                // Variable in map format
                variableMatch = gradleFileContents[dotGradleFileName] =~
                        update.variableUseMapFormatMatchString()
                getVariablesFromMatches(variableMatch, versionVariables, update, problemVariables)

                // Variable in dependencySet format
                variableMatch = gradleFileContents[dotGradleFileName] =~
                        update.variableInDependencySetString()
                getVariablesFromMatches(variableMatch, versionVariables, update, problemVariables)

                // Variable in unnamed Kotlin notation
                variableMatch = gradleFileContents[dotGradleFileName] =~
                        update.variableKotlinUnnamedParametersMatchString()
                getVariablesFromMatches(variableMatch, versionVariables, update, problemVariables)

                // Variable in named Kotlin notation
                update.variableKotlinSeparateNamedParametersMatchString().each { String it ->
                    variableMatch = gradleFileContents[dotGradleFileName] =~ it
                    getVariablesFromMatches(variableMatch, versionVariables, update, problemVariables)
                }
            }
        }
        versionVariables
    }

    static String getDependencyUpdatesJsonReportFilePath(Project project) {
        Task dependencyUpdatesTask = project.tasks.getByPath('dependencyUpdates')
        String outputDir = dependencyUpdatesTask.properties['outputDir'] as String
        outputDir = ensureOutputDirIsAbsolute(outputDir, project)
        String reportfileName = dependencyUpdatesTask.properties['reportfileName'] as String
        outputDir + File.separator + reportfileName + '.json'
    }

    private static String ensureOutputDirIsAbsolute(String outputDir, Project project) {
        if (!outputDir.startsWith('/')) {
            return project.buildDir.parent + File.separator + outputDir
        }
        outputDir
    }
}

