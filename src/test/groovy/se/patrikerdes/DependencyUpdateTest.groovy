package se.patrikerdes

import spock.lang.Specification

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

    def "Regex to match Kotlin named parameters"(String input, boolean matches) {
        expect:

        String parameterName = '\\w*'
        String valueWithQuotes = '\"[^\"]*\"'
        String valueWithoutQuotes = '[^\"\\s]+'
        String parameterValue = "(?:$valueWithQuotes|$valueWithoutQuotes)"
        String additionalParameter = "(?:\\s*$parameterName\\s*=\\s*$parameterValue\\s*,?\\s*)"
        String regex =
                "\\(" +
                    "\\s*$additionalParameter*" +
                        "(?:" +
                            "(?:" +
                                "(?:group|name)\\s*=\\s*$parameterValue\\s*,?\\s*|" +
                                     "version\\s*=\\s*$parameterValue\\s*,?\\s*" +
                            ")" +
                        "(?!.*\\1)){3}" +
                    "$additionalParameter*" +
                "\\)"

        input.matches(regex) == matches

        where:
        input                                                                          | matches
        // Allowed whitespaces
        '(group = "group", name = "name", version = "oldVersion")'                     | true
        '(group="group",name="name",version="oldVersion")'                             | true
        '(   group    ="group"     ,    name   =  "name" ,version =  "oldVersion"   )' | true
        // Variables instead of string values
        '(group = group, name = name, version = oldVersion)'                           | true
        '(group = "group", name = name, version = "oldVersion")'                       | true
        // Missing matching quote
        '(group = "group, name = name, version = oldVersion)'                          | false
        '(group = group, name = name, version = oldVersion")'                          | false
        // Parameter name in quotes
        '("group" = group, name = name, version = oldVersion)'                         | false
        // Missing parameter
        '(name = name, version = oldVersion)'                                          | false
        '(group = group, version = oldVersion)'                                        | false
        '(group = group, name = name)'                                                 | false
        // Additional parameter (first, last, middle)
        '(ext = "ext", group = group, name = name, version = oldVersion)'              | true
        // NOT WORKING: Additional parameter in between
        //'(group = group, name = name, ext = "ext", version = oldVersion)'              | true
        '(group = group, name = name, version = oldVersion, ext = "ext")'              | true
        // Permutations
        '(group = group, version = oldVersion, name = name)'                           | true
        '(version = oldVersion, name = name, group = group)'                           | true
        '(version = oldVersion, group = group, name = name)'                           | true
        '(name = name, version = oldVersion, group = group)'                           | true
        '(name = name, group = group, version = oldVersion)'                           | true
        // NOT WORKING: 2 groups with different values, no name
        //'(group = group, group = name, version = oldVersion)'                          | false
    }
}
