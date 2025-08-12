package se.patrikerdes

import static se.patrikerdes.UseLatestVersionsPlugin.USE_LATEST_VERSIONS

import org.gradle.api.Project
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class InternalAggregateRootTask extends DefaultTask {
    protected static List<String> rootVersionFiles = Collections.emptyList()

    static void setRootVersionFiles(List<String> versionFiles) {
        rootVersionFiles = versionFiles
    }

    InternalAggregateRootTask() {
        description = 'Internal task that aggregates versions of all projects to root. ' +
                'It updates the files listed in rootVersionFiles. Don\'t run it as separate task'
    }

    @TaskAction
    void internalAggregateRootTask() {
        List<String> versionVariablesFiles = project.gradle.taskGraph.allTasks
                .findAll { it.name == USE_LATEST_VERSIONS }
                .collectMany { getVersionVariablesFiles(it.project) }
        List<String> dotGradleFileNames = rootVersionFiles

        Map<String, String> gradleFileContents = dotGradleFileNames.collectEntries {
            [(it): new File(it).getText('UTF-8')]
        }
        Map<String, String> versionVariables = readVersionVariables(versionVariablesFiles)
        Common.updateVersionVariables(gradleFileContents, dotGradleFileNames, versionVariables)

        // Write all files back
        for (dotGradleFileName in dotGradleFileNames) {
            new File(dotGradleFileName).setText(gradleFileContents[dotGradleFileName], 'UTF-8')
        }

        // Delete temp files in build folder
        for (String versionVariablesFile in versionVariablesFiles) {
            new File(versionVariablesFile).delete()
        }
    }

    @SuppressWarnings(['NoDef', 'VariableTypeRequired', 'UnnecessaryGetter'])
    List<String> getVersionVariablesFiles(Project project) {
        String buildDir = project.buildDir.absolutePath

        Class finderClass
        try {
            // Groovy 4.x location
            finderClass = this.class.classLoader.loadClass('groovy.ant.FileNameFinder')
        } catch (ClassNotFoundException e) {
            // Groovy 2.x / 3.x location
            finderClass = this.class.classLoader.loadClass('groovy.util.FileNameFinder')
        }
        def finder = finderClass.getDeclaredConstructor().newInstance()
        finder.getFileNames(buildDir, 'useLatestVersions/version-variables.json')
    }

    Map<String, String> readVersionVariables(List<String> versionVariablesFiles) {
        Map<String, String> versionVariables = [:]
        List<String> problemVariables = []
        for (String versionVariablesFile in versionVariablesFiles) {
            Map<String, String> variables = new JsonSlurper().parseText(new File(versionVariablesFile).text)
            variables.forEach { k, v ->
                if (versionVariables.containsKey(k) && versionVariables.get(k) != v) {
                    println("A problem was detected: the variable '$k' has different updated versions in different " +
                            "projects.\nNew updated versions are: '${versionVariables.get(k)}' and '$v', " +
                            "root config file value won't be be changed.")
                    problemVariables.add(k)
                } else {
                    versionVariables.put(k, v)
                }
            }
        }
        for (problemVariable in problemVariables) {
            versionVariables.remove(problemVariable)
        }
        versionVariables
    }

}
