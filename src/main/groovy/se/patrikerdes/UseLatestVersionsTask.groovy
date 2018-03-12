package se.patrikerdes

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.regex.Matcher

import static se.patrikerdes.Common.getOutDatedDependencies

@CompileStatic
class UseLatestVersionsTask extends DefaultTask {
    String variablDefinitionMatchString(String variable) {
        return "(" + variable + "[ \\t]+=[ \t]*?[\"'])(.*)([\"'])"
    }

    String newVariableDefinitionString(String newVersion) {
        return '$1' + newVersion + '$3'
    }

    @TaskAction
    def useLatestVersions() {
        // Save a copy of the json report from dependencyUpdates, for use by the check task
        File useLatestVersionsFolder = new File(project.rootDir, 'build' + File.separator + 'useLatestVersions')
        if(!useLatestVersionsFolder.exists()) {
            useLatestVersionsFolder.mkdirs()
        }
        File dependencyUpdatesJsonReportFile = new File(project.rootDir, 'build' + File.separator + 'dependencyUpdates' + File.separator + 'report.json')
        Files.copy(dependencyUpdatesJsonReportFile.toPath(), new File(useLatestVersionsFolder, 'latestDependencyUpdatesReport.json').toPath(), StandardCopyOption.REPLACE_EXISTING)

        // Get all *.gradle files
        List<String> dotGradleFileNames = new FileNameFinder().getFileNames(project.rootDir.getAbsolutePath(), "**/*.gradle")
        // TODO: Also get *.gradle.kts files

        // Read the json report from dependencyUpdates
        def dependencyUpdatesJson = new JsonSlurper().parse(dependencyUpdatesJsonReportFile)

        // Get outdated dependencies
        List<DependencyUpdate> dependecyUpdates = getOutDatedDependencies(dependencyUpdatesJson)

        // Get current dependencies
        def currentDependencies = dependencyUpdatesJson['current']['dependencies']
        def dependencyStables = []
        for(currentDependency in currentDependencies) {
            dependencyStables.add(new DependencyUpdate(currentDependency['group'], currentDependency['name'], currentDependency['version'], currentDependency['version']))
        }

        Map<String, String> gradleFileContents = [:]

        // Pass 1: Get the content of all *.gradle files
        for(String dotGradleFileName in dotGradleFileNames) {
            def currentGradleFileContents = new File(dotGradleFileName).getText('UTF-8')
            gradleFileContents[dotGradleFileName] = currentGradleFileContents
        }

        // Pass 2: Update module versions
        for(String dotGradleFileName in dotGradleFileNames) {
            for(DependencyUpdate update in dependecyUpdates) {
                // String notation
                gradleFileContents[dotGradleFileName] = gradleFileContents[dotGradleFileName].replaceAll(update.oldModuleVersionStringFormatMatchString(), update.newVersionString())
                // Map notation
                gradleFileContents[dotGradleFileName] = gradleFileContents[dotGradleFileName].replaceAll(update.oldModuleVersionMapFormatMatchString(), update.newVersionString())
            }
        }

        // Pass 3: Update plugin versions
        for(String dotGradleFileName in dotGradleFileNames) {
            for(DependencyUpdate update in dependecyUpdates) {
                gradleFileContents[dotGradleFileName] = gradleFileContents[dotGradleFileName].replaceAll(update.oldPluginVersionMatchString(), update.newVersionString())
            }
        }

        // Pass 4: Find variables with version numbers
        Map<String, String> versionVariables = [:]
        Set problemVariables = []

        for(String dotGradleFileName in dotGradleFileNames) {
            for(DependencyUpdate update in dependecyUpdates + dependencyStables) {
                Matcher variableMatch = gradleFileContents[dotGradleFileName] =~ update.variableUseStringFormatMatchString()
                if(variableMatch.size() == 1) {
                    String variableName = ((List)variableMatch[0])[1]
                    if(versionVariables.containsKey(variableName) && versionVariables[variableName] != update.newVersion) {
                        println("A problem was detected: $variableName is supposed to be updated to both ${versionVariables[variableName]} and ${update.newVersion}, it won't be changed.")
                        problemVariables.add(variableName)
                    } else {
                        versionVariables.put(variableName, update.newVersion)
                    }
                }

                variableMatch = gradleFileContents[dotGradleFileName] =~ update.variableUseMapFormatMatchString()
                if(variableMatch.size() == 1) {
                    String variableName = ((List)variableMatch[0])[1]
                    if(versionVariables.containsKey(variableName) && versionVariables[variableName] != update.newVersion) {
                        println("A problem was detected: $variableName is supposed to be updated to both ${versionVariables[variableName]} and ${update.newVersion}, it won't be changed.")
                        problemVariables.add(variableName)
                    } else {
                        versionVariables.put(variableName, update.newVersion)
                    }
                }
            }
        }

        for(problemVariable in problemVariables) {
            versionVariables.remove(problemVariable)
        }

        // Pass 5: Exclude variables defined more than once
        Set variableDefinitions = []
        problemVariables = []

        for(String dotGradleFileName in dotGradleFileNames) {
            for (variableName in versionVariables.keySet()) {
                Matcher variableDefinitionMatch = gradleFileContents[dotGradleFileName] =~ variablDefinitionMatchString(variableName)
                if(variableDefinitionMatch.size() == 1) {
                    if(variableDefinitions.contains(variableName)) {
                        // The variable is assigned to in more than one file
                        println("A problem was detected: the variable $variableName is assigned more than once and won't be changed.")
                        problemVariables.add(variableName)
                    } else {
                        variableDefinitions.add(variableName)
                    }
                } else if(variableDefinitionMatch.size() > 1) {
                    // The variable is assigned to more than once in the same file
                    println("A problem was detected: the variable $variableName is assigned more than once and won't be changed.")
                    problemVariables.add(variableName)
                }
            }
        }

        for(problemVariable in problemVariables) {
            versionVariables.remove(problemVariable)
        }

        // Pass 6: Update variables
        for(String dotGradleFileName in dotGradleFileNames) {
            for (versionVariable in versionVariables) {
                gradleFileContents[dotGradleFileName] = gradleFileContents[dotGradleFileName].replaceAll(variablDefinitionMatchString(versionVariable.getKey()), newVariableDefinitionString(versionVariable.getValue()))
            }
        }

        // Pass 7: Write all files back
        for(dotGradleFileName in dotGradleFileNames) {
            new File(dotGradleFileName).setText(gradleFileContents[dotGradleFileName], 'UTF-8')
        }
    }
}
