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
}
