package se.patrikerdes

import spock.lang.Specification

import java.util.regex.Matcher

class DependencyUpdateTest extends Specification {
    private final DependencyUpdate update = new DependencyUpdate('group', 'name', 'oldVersion', 'newVersion')

    void "oldModuleVersionKotlinSepareteUnnamedParametersMatchString"(String input) {
        expect:
        String matchString = update.oldModuleVersionKotlinUnnamedParametersMatchString()
        String output = input.replaceAll(matchString, update.newVersionString())

        output == input.replace('oldVersion', 'newVersion')

        where:
        input                                                                       | _
        '''testRuntimeOnly("group", "name", "oldVersion")'''                        | _
        '''testRuntimeOnly ( "group" ,"name",     "oldVersion")'''                  | _
        '''implementation("group", "name", "oldVersion")'''                         | _
        '''annotationProcessor("group", "name", "oldVersion")'''                    | _
        '''api("group", "name", "oldVersion")'''                                    | _
        '''apiDependenciesMetadata("group", "name", "oldVersion")'''                | _
        '''apiElements("group", "name", "oldVersion")'''                            | _
        '''compile("group", "name", "oldVersion")'''                                | _
        '''compileClasspath("group", "name", "oldVersion")'''                       | _
        '''compileOnly("group", "name", "oldVersion")'''                            | _
        '''compileOnlyDependenciesMetadata("group", "name", "oldVersion")'''        | _
        '''implementation("group", "name", "oldVersion")'''                         | _
        '''implementationDependenciesMetadata("group", "name", "oldVersion")'''     | _
        '''runtime("group", "name", "oldVersion")'''                                | _
        '''runtimeClasspath("group", "name", "oldVersion")'''                       | _
        '''runtimeElements("group", "name", "oldVersion")'''                        | _
        '''runtimeOnly("group", "name", "oldVersion")'''                            | _
        '''runtimeOnlyDependenciesMetadata("group", "name", "oldVersion")'''        | _
        '''testAnnotationProcessor("group", "name", "oldVersion")'''                | _
        '''testApi("group", "name", "oldVersion")'''                                | _
        '''testApiDependenciesMetadata("group", "name", "oldVersion")'''            | _
        '''testCompile("group", "name", "oldVersion")'''                            | _
        '''testCompileClasspath("group", "name", "oldVersion")'''                   | _
        '''testCompileOnly("group", "name", "oldVersion")'''                        | _
        '''testCompileOnlyDependenciesMetadata("group", "name", "oldVersion")'''    | _
        '''testImplementation("group", "name", "oldVersion")'''                     | _
        '''testImplementationDependenciesMetadata("group", "name", "oldVersion")''' | _
        '''testKotlinScriptDef("group", "name", "oldVersion")'''                    | _
        '''testKotlinScriptDefExtensions("group", "name", "oldVersion")'''          | _
        '''testRuntime("group", "name", "oldVersion")'''                            | _
        '''testRuntimeClasspath("group", "name", "oldVersion")'''                   | _
        '''testRuntimeOnlyDependenciesMetadata("group", "name", "oldVersion")'''    | _
    }

    void "oldModuleVersionKotlinSepareteNamedParametersMatchString"(String input) {
        expect:
        String output = ''
        String tempInput = input
        update.oldModuleVersionKotlinSeparateNamedParametersMatchString().forEach {
            output = tempInput.replaceAll(it, update.newVersionString())
            tempInput = output
        }
        output == input.replace('oldVersion', 'newVersion')

        where:
        input                                                                          | _
        // Allowed whitespaces
        '(group = "group", name = "name", version = "oldVersion")'                     | _
        '(group="group",name="name",version="oldVersion")'                             | _
        '(   group    ="group"     ,    name   =  "name" ,version =  "oldVersion"   )' | _
        // Variables for group and name instead of string
        '(group = "group", name = "name", version = "oldVersion")'                     | _
        // Additional parameter
        '(ext = "ext", group = "group", name = "name", version = "oldVersion")'        | _
        '(group = "group", name = "name", version = "oldVersion", ext = "ext")'        | _
        // Permutations
        '(group = "group", name = "name", version = "oldVersion")'                     | _
        '(group = "group", version = "oldVersion", name = "name")'                     | _
        '(name = "name", version = "oldVersion", group = "group")'                     | _
        '(name = "name", group = "group", version = "oldVersion")'                     | _
        '(version = "oldVersion", name = "name", group = "group")'                     | _
        '(version = "oldVersion", group = "group", name = "name")'                     | _
    }

    void "variableKotlinSeparateNamedParametersMatchString"(String input) {
        expect:
        Matcher variableMatch
        List<String> versionVariables = []
        update.variableKotlinSeparateNamedParametersMatchString().each { String it ->
            variableMatch = input =~ it
            String variableName = getVariableFromMatches(variableMatch)
            if (variableName != null) {
                versionVariables.add(variableName)
            }
        }

        input && versionVariables.size() == 1 && versionVariables.first() == 'oldVersion'

        where:
        input                                                                        | _
        // Allowed whitespaces
        '(group = "group", name = "name", version = oldVersion)'                     | _
        '(group="group",name="name",version=oldVersion)'                             | _
        '(   group    ="group"     ,    name   =  "name" ,version =  oldVersion   )' | _
        // Variables for group and name instead of string
        '(group = "group", name = "name", version = oldVersion)'                     | _
        // Additional parameter
        '(ext = "ext", group = "group", name = "name", version = oldVersion)'        | _
        '(group = "group", name = "name", version = oldVersion, ext = "ext")'        | _
        // Permutations
        '(group = "group", name = "name", version = oldVersion)'                     | _
        '(group = "group", version = oldVersion, name = "name")'                     | _
        '(name = "name", version = oldVersion, group = "group")'                     | _
        '(name = "name", group = "group", version = oldVersion)'                     | _
        '(version = oldVersion, name = "name", group = "group")'                     | _
        '(version = oldVersion, group = "group", name = "name")'                     | _
        'testCompile(group = "group", name = "name", version = oldVersion)'          | _
    }

    private static String getVariableFromMatches(Matcher variableMatch) {
        if (variableMatch.size() == 1) {
            String variableName = ((List) variableMatch[0])[1]
            return variableName
        }
        null
    }
}
