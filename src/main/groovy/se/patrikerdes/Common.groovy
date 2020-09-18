package se.patrikerdes

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task

import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

@CompileStatic
class Common {
    public static final String WHITE_BLACKLIST_ERROR_MESSAGE = 'Using both, --update-dependency and ' +
            '--ignore-dependency, is not allowed.'

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
        // File can have more dependencies with same version variable
        // We anyway check that versions of dependencies for that variable are the same
        if (variableMatch.size() >= 1) {
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
        String reportfileName = dependencyUpdatesTask.properties['reportfileName'] ?: 'report'
        outputDir + File.separator + reportfileName + '.json'
    }

    private static String ensureOutputDirIsAbsolute(String outputDir, Project project) {
        if (!Paths.get(outputDir).isAbsolute()) {
            return project.buildDir.parent + File.separator + outputDir
        }
        outputDir
    }

    static void updateVersionVariables(Map<String, String> gradleFileContents, List<String> dotGradleFileNames,
                                       Map<String, String> versionVariables) {
        for (String dotGradleFileName in dotGradleFileNames) {
            for (versionVariable in versionVariables) {
                gradleFileContents[dotGradleFileName] =
                        gradleFileContents[dotGradleFileName].replaceAll(
                                variableDefinitionMatchStringForFileName(versionVariable.key, dotGradleFileName),
                                newVariableDefinitionString(versionVariable.value))
            }
        }
    }

    static String variableDefinitionMatchStringForFileName(String variable, String fileName) {
        String splitter = File.separator.replace('\\', '\\\\')
        if (fileName.split(splitter).last() == 'gradle.properties') {
            return gradlePropertiesVariableDefinitionMatchString(variable)
        }
        variableDefinitionMatchString(variable)
    }

    static String variableDefinitionMatchString(String variable) {
        '(' + Pattern.quote(variable) + "[ \t]*=[ \t]*[\"'])(.*)([\"'])"
    }

    static String gradlePropertiesVariableDefinitionMatchString(String variable) {
        '(' + Pattern.quote(variable) + '[ \t]*=[ \t]*)(.*)([ \t]*)'
    }

    static String newVariableDefinitionString(String newVersion) {
        '$1' + newVersion + '$3'
    }

    static List<String> getGradleConfigFilesOnPath(String absolutePath) {
        List<String> filePaths = new FileNameFinder().getFileNames(absolutePath, '**/*.gradle')
        filePaths += new FileNameFinder().getFileNames(absolutePath, '**/gradle.properties')
    }

    static List<String> getKotlinConfigFilesOnPath(String absolutePath) {
        List<String> filePaths = new FileNameFinder().getFileNames(absolutePath, '**/*.gradle.kts')
        filePaths += new FileNameFinder().getFileNames(absolutePath, 'buildSrc/**/*.kt')
    }

}

