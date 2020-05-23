package se.patrikerdes

import static se.patrikerdes.Common.WHITE_BLACKLIST_ERROR_MESSAGE
import static se.patrikerdes.Common.getCurrentDependencies
import static se.patrikerdes.Common.getDependencyUpdatesJsonReportFilePath
import static se.patrikerdes.Common.getOutDatedDependencies

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.regex.Matcher
import java.util.regex.Pattern

@CompileStatic
class UseLatestVersionsTask extends DefaultTask {
    @Input
    @Option(option='update-dependency',
            description = 'A whitelist of dependencies to update, in the format of group:name')
    List<String> updateWhitelist = Collections.emptyList()

    @Input
    @Option(option='ignore-dependency',
            description = 'A blacklist of dependencies to update, in the format of group:name')
    List<String> updateBlacklist = Collections.emptyList()

    UseLatestVersionsTask() {
        description = 'Updates module and plugin versions in all *.gradle and *.gradle.kts files to the latest ' +
                'available versions.'
        group = 'Help'
    }

    String variableDefinitionMatchString(String variable) {
        '(' + Pattern.quote(variable) + "[ \t]*=[ \t]*[\"'])(.*)([\"'])"
    }

    String gradlePropertiesVariableDefinitionMatchString(String variable) {
        '(' + Pattern.quote(variable) + '[ \t]*=[ \t]*)(.*)([ \t]*)'
    }

    String newVariableDefinitionString(String newVersion) {
        '$1' + newVersion + '$3'
    }

    @TaskAction
    void useLatestVersions() {
        validateExclusiveWhiteOrBlacklist()
        File dependencyUpdatesJsonReportFile = new File(getDependencyUpdatesJsonReportFilePath(project))
        saveDependencyUpdatesReport(dependencyUpdatesJsonReportFile)

        List<String> dotGradleFileNames =
                new FileNameFinder().getFileNames(project.projectDir.absolutePath, '**/*.gradle')
        dotGradleFileNames += new FileNameFinder().getFileNames(project.projectDir.absolutePath, '**/*.gradle.kts')
        dotGradleFileNames += new FileNameFinder().getFileNames(project.projectDir.absolutePath, '**/gradle.properties')
        dotGradleFileNames += new FileNameFinder().getFileNames(project.projectDir.absolutePath, 'buildSrc/**/*.kt')

        // Exclude any files that belong to sub-projects
        List<String> subprojectPaths = project.subprojects.collect { it.projectDir.absolutePath }
        dotGradleFileNames = dotGradleFileNames.findAll { dotGradleFileName ->
            !subprojectPaths.any { subprojectPath -> dotGradleFileName.startsWith(subprojectPath) }
        }

        Object dependencyUpdatesJson = new JsonSlurper().parse(dependencyUpdatesJsonReportFile)

        List<DependencyUpdate> dependencyUpdates = getOutDatedDependencies(dependencyUpdatesJson)
        if (!updateWhitelist.empty) {
            dependencyUpdates = dependencyUpdates.findAll {
                updateWhitelist.contains(it.groupAndName()) || updateWhitelist.contains(it.group)
            }
        }
        if (!updateBlacklist.empty) {
            dependencyUpdates = dependencyUpdates.findAll {
                !updateBlacklist.contains(it.groupAndName()) && !updateBlacklist.contains(it.group)
            }
        }

        List<DependencyUpdate> dependencyStables = getCurrentDependencies(dependencyUpdatesJson)

        Map<String, String> gradleFileContents = [:]

        for (String dotGradleFileName in dotGradleFileNames) {
            String currentGradleFileContents = new File(dotGradleFileName).getText('UTF-8')
            gradleFileContents[dotGradleFileName] = currentGradleFileContents
        }

        updateModuleVersions(gradleFileContents, dotGradleFileNames, dependencyUpdates)
        updatePluginVersions(gradleFileContents, dotGradleFileNames, dependencyUpdates)
        updateVariables(gradleFileContents, dotGradleFileNames, dependencyUpdates, dependencyStables)

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

                // dependencySet notation
                gradleFileContents[dotGradleFileName] =
                        gradleFileContents[dotGradleFileName].replaceAll(
                                update.oldModuleVersionDependencySetString(), update.newVersionString())

                // Kotlin unnamed notation
                gradleFileContents[dotGradleFileName] =
                        gradleFileContents[dotGradleFileName].replaceAll(
                                update.oldModuleVersionKotlinUnnamedParametersMatchString(), update.newVersionString())

                // Kotlin named notation
                update.oldModuleVersionKotlinSeparateNamedParametersMatchString().each { String it ->
                    gradleFileContents[dotGradleFileName] =
                            gradleFileContents[dotGradleFileName].replaceAll(
                                    it, update.newVersionString())
                }
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
        Set problemVariables = []
        Map<String, String> versionVariables = Common.findVariables(dotGradleFileNames,
                dependencyUpdates + dependencyStables, gradleFileContents, problemVariables)

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
        String splitter = File.separator.replace('\\', '\\\\')
        if (fileName.split(splitter).last() == 'gradle.properties') {
            return gradlePropertiesVariableDefinitionMatchString(variable)
        }
        variableDefinitionMatchString(variable)
    }

    void saveDependencyUpdatesReport(File dependencyUpdatesJsonReportFile) {
        File useLatestVersionsFolder = new File(project.buildDir, 'useLatestVersions')
        if (!useLatestVersionsFolder.exists()) {
            useLatestVersionsFolder.mkdirs()
        }
        Files.copy(dependencyUpdatesJsonReportFile.toPath(),
                new File(useLatestVersionsFolder, 'latestDependencyUpdatesReport.json').toPath(),
                StandardCopyOption.REPLACE_EXISTING)
    }

    private void validateExclusiveWhiteOrBlacklist() {
        if (!updateWhitelist.empty && !updateBlacklist.empty) {
            throw new GradleException(WHITE_BLACKLIST_ERROR_MESSAGE)
        }
    }
}
