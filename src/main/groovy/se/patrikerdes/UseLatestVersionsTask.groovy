package se.patrikerdes

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

@CompileStatic
class UseLatestVersionsTask extends DefaultTask {
    @groovy.transform.TupleConstructor
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
        String oldModuleVersionVariableMatchString() {
            return "[\"']" + this.group + ":" + this.name + ":[\$](.*)[\"']"
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
    }

    @TaskAction
    def useLatestVersions() {
        def dotGradleFiles = new groovy.util.FileNameFinder().getFileNames(project.rootDir.getAbsolutePath(), "**/*.gradle")

        def dependencyUpdatesJsonReportFile = new File(project.rootDir, 'build' + File.separator + 'dependencyUpdates' + File.separator + 'report.json')
        def dependencyUpdatesJson = new groovy.json.JsonSlurper().parse(dependencyUpdatesJsonReportFile)
        def outdatedDependencies = dependencyUpdatesJson['outdated']['dependencies']
        def dependecyUpdates = []
        for(outdatedDependency in outdatedDependencies) {
            dependecyUpdates.add(new DependencyUpdate(outdatedDependency['group'], outdatedDependency['name'], outdatedDependency['version'], outdatedDependency['available']['milestone']))
        }

//        def versionVariables = [:]
//        Set problemVariables = []

        for(String dotGradleFile in dotGradleFiles) {
            def gradleFileContents = new File(dotGradleFile).getText('UTF-8')
            for(DependencyUpdate update in dependecyUpdates) {
                // Update module versions
                gradleFileContents = gradleFileContents.replaceAll(update.oldModuleVersionMatchString(), update.newModuleVersionString())

                // Update plugin versions
                gradleFileContents = gradleFileContents.replaceAll(update.oldPluginVersionMatchString(), update.newPluginVersionString())
//
//                // Find variables with versions
//                def variableMatch = gradleFileContents =~ update.oldModuleVersionVariableMatchString()
//                if(variableMatch.size() == 1) {
//                    def variableName = variableMatch[0][1]
//                    if(versionVariables.containsKey(variableName) && versionVariables[variableName] != update.newVersion) {
//                        println("A problem was detected: $variableName is supposed to be updated to both ${versionVariables[variableName]} and ${update.newVersion}, it won't be changed. Figure it out.")
//                        problemVariables.add(variableName)
//                    } else {
//                        versionVariables.put(variableName, update.newVersion)
//                    }
//                }
            }
            new File(dotGradleFile).setText(gradleFileContents, 'UTF-8')
        }

//        // Todo: thee passes over the files, without saving the files twice (store file contents in map)
//        // Todo: in the second pass, exclude variables defined more than once
//        // Todo: in the third pass, update variables
//
//        for(problemVariable in problemVariables) {
//            versionVariables.remove(problemVariable)
//        }
//        println(versionVariables)
    }
}
