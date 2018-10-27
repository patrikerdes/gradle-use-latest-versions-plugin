package se.patrikerdes

import static se.patrikerdes.Common.getCurrentDependencies
import static se.patrikerdes.Common.getOutDatedDependencies

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.regex.Matcher

@CompileStatic
class UseLatestVersionsTask extends DefaultTask {
    UseLatestVersionsTask() {
        description = 'Updates module and plugin versions in all *.gradle and *.gradle.kts files to the latest ' +
                'available versions.'
        group = 'Help'
    }

    String variableDefinitionMatchString(String variable) {
        '(' + variable + "[ \t]*=[ \t]*[\"'])(.*)([\"'])"
    }

    String gradlePropertiesVariableDefinitionMatchString(String variable) {
        '(' + variable + "[ \t]*=[ \t]*)(.*)([ \t]*\$)"
    }

    String newVariableDefinitionString(String newVersion) {
        '$1' + newVersion + '$3'
    }

    @TaskAction
    void useLatestVersions() {
        File dependencyUpdatesJsonReportFile = new File(project.rootDir, 'build' + File.separator +
                'dependencyUpdates' + File.separator + 'report.json')
        saveDependencyUpdatesReport(dependencyUpdatesJsonReportFile)

        List<String> dotGradleFileNames =
                new FileNameFinder().getFileNames(project.rootDir.absolutePath, '**/*.gradle')
        dotGradleFileNames += new FileNameFinder().getFileNames(project.rootDir.absolutePath, '**/*.gradle.kts')
        dotGradleFileNames += new FileNameFinder().getFileNames(project.rootDir.absolutePath, '**/gradle.properties')

        Object dependencyUpdatesJson = new JsonSlurper().parse(dependencyUpdatesJsonReportFile)

        List<DependencyUpdate> dependecyUpdates = getOutDatedDependencies(dependencyUpdatesJson)

        List<DependencyUpdate> dependencyStables = getCurrentDependencies(dependencyUpdatesJson)

        Map<String, String> gradleFileContents = [:]

        for (String dotGradleFileName in dotGradleFileNames) {
            String currentGradleFileContents = new File(dotGradleFileName).getText('UTF-8')
            gradleFileContents[dotGradleFileName] = currentGradleFileContents
        }

        updateModuleVersions(gradleFileContents, dotGradleFileNames, dependecyUpdates)
        updatePluginVersions(gradleFileContents, dotGradleFileNames, dependecyUpdates)
        updateVariables(gradleFileContents, dotGradleFileNames, dependecyUpdates, dependencyStables)

        // Write all files back
        for (dotGradleFileName in dotGradleFileNames) {
            new File(dotGradleFileName).setText(gradleFileContents[dotGradleFileName], 'UTF-8')
        }
    }

    void updateModuleVersions(Map<String, String> gradleFileContents, List<String> dotGradleFileNames,
                              List<DependencyUpdate> dependencyUpdates) {
        for (String dotGradleFileName in dotGradleFileNames) {
            for (DependencyUpdate update in dependencyUpdates) {
                // String notation
                gradleFileContents[dotGradleFileName] =
                        gradleFileContents[dotGradleFileName].replaceAll(
                                update.oldModuleVersionStringFormatMatchString(), update.newVersionString())
                // Map notation
                gradleFileContents[dotGradleFileName] =
                        gradleFileContents[dotGradleFileName].replaceAll(
                                update.oldModuleVersionMapFormatMatchString(), update.newVersionString())
            }
        }
    }

    void updatePluginVersions(Map<String, String> gradleFileContents, List<String> dotGradleFileNames,
                              List<DependencyUpdate> dependencyUpdates) {
        for (String dotGradleFileName in dotGradleFileNames) {
            for (DependencyUpdate update in dependencyUpdates) {
                gradleFileContents[dotGradleFileName] =
                        gradleFileContents[dotGradleFileName].replaceAll(
                                update.oldPluginVersionMatchString(), update.newVersionString())
            }
        }
    }

    void updateVariables(Map<String, String> gradleFileContents, List<String> dotGradleFileNames,
                         List<DependencyUpdate> dependencyUpdates, List<DependencyUpdate> dependencyStables) {
        // Find variables with version numbers
        Map<String, String> versionVariables = [:]
        Set problemVariables = []

        for (String dotGradleFileName in dotGradleFileNames) {
            for (DependencyUpdate update in dependencyUpdates + dependencyStables) {
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
            }
        }

        for (problemVariable in problemVariables) {
            versionVariables.remove(problemVariable)
        }

        // Exclude variables defined more than once
        Set variableDefinitions = []
        problemVariables = []

        for (String dotGradleFileName in dotGradleFileNames) {
            for (variableName in versionVariables.keySet()) {
                Matcher variableDefinitionMatch = gradleFileContents[dotGradleFileName] =~
                        variableDefinitionMatchStringForFileName(variableName, dotGradleFileName)
                if (variableDefinitionMatch.size() == 1) {
                    if (variableDefinitions.contains(variableName)) {
                        // The variable is assigned to in more than one file
                        println("A problem was detected: the variable $variableName is assigned more than once and " +
                                "won't be changed.")
                        problemVariables.add(variableName)
                    } else {
                        variableDefinitions.add(variableName)
                    }
                } else if (variableDefinitionMatch.size() > 1) {
                    // The variable is assigned to more than once in the same file
                    println("A problem was detected: the variable $variableName is assigned more than once and won't " +
                            'be changed.')
                    problemVariables.add(variableName)
                }
            }
        }

        for (problemVariable in problemVariables) {
            versionVariables.remove(problemVariable)
        }

        // Update variables
        for (String dotGradleFileName in dotGradleFileNames) {
            for (versionVariable in versionVariables) {
                gradleFileContents[dotGradleFileName] =
                        gradleFileContents[dotGradleFileName].replaceAll(
                                variableDefinitionMatchStringForFileName(versionVariable.key, dotGradleFileName),
                                newVariableDefinitionString(versionVariable.value))
            }
        }
    }

    String variableDefinitionMatchStringForFileName(String variable, String fileName) {
        if (fileName.split(File.separator).last() == 'gradle.properties') {
            gradlePropertiesVariableDefinitionMatchString(variable)
        }
        variableDefinitionMatchString(variable)
    }

    void saveDependencyUpdatesReport(File dependencyUpdatesJsonReportFile) {
        File useLatestVersionsFolder = new File(project.rootDir, 'build' + File.separator + 'useLatestVersions')
        if (!useLatestVersionsFolder.exists()) {
            useLatestVersionsFolder.mkdirs()
        }
        Files.copy(dependencyUpdatesJsonReportFile.toPath(),
                new File(useLatestVersionsFolder, 'latestDependencyUpdatesReport.json').toPath(),
                StandardCopyOption.REPLACE_EXISTING)
    }

    void getVariablesFromMatches(Matcher variableMatch, Map<String, String> versionVariables, DependencyUpdate update,
                                 Set problemVariables) {
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
}
