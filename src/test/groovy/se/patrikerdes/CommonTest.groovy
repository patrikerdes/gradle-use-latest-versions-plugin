package se.patrikerdes

import static se.patrikerdes.Common.findVariables

import spock.lang.Specification

class CommonTest extends Specification {
    private final DependencyUpdate update = new DependencyUpdate('junit', 'junit', 'oldVersion', 'newVersion')

    void "ParseVariables"(String fileContent) {
        expect:
        String fileName = 'build.gradle.kts'
        Set problemVariable = []

        List<String> fileNames = [fileName]
        List<DependencyUpdate> dependencyUpdates = [update]
        Map<String, String> fileContents = [(fileName): fileContent]
        Map<String, String> variables = findVariables(fileNames, dependencyUpdates, fileContents, problemVariable)

        fileContent && variables.size() == 1 && variables['junitVersion'] == 'newVersion' && problemVariable.size() == 0

        where:
        fileContent                                                            | _
        'testCompile(group = "junit", name = "junit", version = junitVersion)' | _
        'testCompile("junit", "junit", junitVersion)'                          | _
    }
}
