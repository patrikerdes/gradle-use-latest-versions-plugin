package se.patrikerdes

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.regex.Matcher

@CompileStatic
class UseLatestVersionsTask extends DefaultTask {
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
            return "([\"']" + this.group + ":" + this.name + ":)[^\$].*([\"'])"
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
            return "[\"']" + this.group + ":" + this.name + ":[\$](.*)[\"']"
        }
    }

    String variablDefinitionMatchString(String variable) {
        return "(" + variable + "[ \\t]+=[ \t]*[\"'])(.*)([\"'])"
    }

    String newVariableDefinitionString(String newVersion) {
        return '$1' + newVersion + '$3'
    }

    @TaskAction
    def useLatestVersions() {
        def dotGradleFileNames = new FileNameFinder().getFileNames(project.rootDir.getAbsolutePath(), "**/*.gradle")

        def dependencyUpdatesJsonReportFile = new File(project.rootDir, 'build' + File.separator + 'dependencyUpdates' + File.separator + 'report.json')
        def dependencyUpdatesJson = new JsonSlurper().parse(dependencyUpdatesJsonReportFile)

        def outdatedDependencies = dependencyUpdatesJson['outdated']['dependencies']
        def dependecyUpdates = []
        for(outdatedDependency in outdatedDependencies) {
            dependecyUpdates.add(new DependencyUpdate(outdatedDependency['group'], outdatedDependency['name'], outdatedDependency['version'], outdatedDependency['available']['milestone']))
        }

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
                gradleFileContents[dotGradleFileName] = gradleFileContents[dotGradleFileName].replaceAll(update.oldModuleVersionMatchString(), update.newModuleVersionString())
            }
        }

        // Pass 3: Update plugin versions
        for(String dotGradleFileName in dotGradleFileNames) {
            for(DependencyUpdate update in dependecyUpdates) {
                gradleFileContents[dotGradleFileName] = gradleFileContents[dotGradleFileName].replaceAll(update.oldPluginVersionMatchString(), update.newPluginVersionString())
            }
        }

        // Pass 4: Find variables with version numbers
        Map<String, String> versionVariables = [:]
        Set problemVariables = []

        for(String dotGradleFileName in dotGradleFileNames) {
            for(DependencyUpdate update in dependecyUpdates + dependencyStables) {
                Matcher variableMatch = gradleFileContents[dotGradleFileName] =~ update.variableUseMatchString()
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

        // Pass 5: Todo: Exclude variables defined more than once
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

        // Pass X: Write all files back
        for(dotGradleFileName in dotGradleFileNames) {
            new File(dotGradleFileName).setText(gradleFileContents[dotGradleFileName], 'UTF-8')
        }
    }
}
