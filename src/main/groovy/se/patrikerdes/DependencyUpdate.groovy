package se.patrikerdes

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@CompileStatic
@TupleConstructor
class DependencyUpdate {
    String group
    String name
    String oldVersion
    String newVersion
    DependencyUpdate(String group, String name, String oldVersion, String newVersion) {
        this.group = group
        this.name = name
        this.oldVersion = oldVersion
        this.newVersion = newVersion
    }
    String oldModuleVersionStringFormatMatchString() {
        "([\"']" + this.group + ':' + this.name + ":)[^\$].*?((:.*?)?[\"'])"
    }
    String oldModuleVersionMapFormatMatchString() {
        "(group[ \\t]*:[ \\t]*[\"']" + this.group + "[\"'][ \\t]*,[ \\t]*name[ \\t]*:[ \\t]*[\"']" + this.name +
                "[\"'][ \\t]*,[ \\t]*version[ \\t]*:[ \\t]*[\"']).*?([\"'])"
    }
    String oldModuleVersionDependencySetString() {
        "(dependencySet\\s*\\(\\s*group\\s*:\\s*[\"']" + this.group +
                "[\"']\\s*,\\s*version\\s*:\\s*[\"'])[^\\s\"']+?([\"'][\\s)]\\s*\\{[^}]*entry\\s*[\"']" +
                this.name + "[\"'])"
    }
    String oldPluginVersionMatchString() {
        "(id[ \\t\\(]+[\"']" + this.group + "[\"'][ \\t\\)]+version[ \\t]+[\"'])" + this.oldVersion + "([\"'])"
    }
    String oldModuleVersionKotlinUnnamedParametersMatchString() {
        '((?:testRuntimeOnly|implementation|annotationProcessor|api|apiDependenciesMetadata|apiElements|compile|' +
                'compileClasspath|compileOnly|compileOnlyDependenciesMetadata|implementation|' +
                'implementationDependenciesMetadata|runtime|runtimeClasspath|runtimeElements|runtimeOnly|' +
                'runtimeOnlyDependenciesMetadata|testAnnotationProcessor|testApi|testApiDependenciesMetadata|' +
                'testCompile|testCompileClasspath|testCompileOnly|testCompileOnlyDependenciesMetadata|' +
                'testImplementation|testImplementationDependenciesMetadata|testKotlinScriptDef|' +
                'testKotlinScriptDefExtensions|testRuntime|testRuntimeClasspath|testRuntimeOnlyDependenciesMetadata)' +
                "\\s*\\(\\s*\"${this.group}\"\\s*,\\s*\"${this.name}\"\\s*,\\s*\")${this.oldVersion}(\"\\s*\\))"
    }
    String parameterName = '\\w*'
    String parameterValueWithQuotes = '\"[^\"]*\"'
    String parameterValueWithoutQuotes = '[^\"\\s,]+'
    String parameterValue = "(?:$parameterValueWithQuotes|$parameterValueWithoutQuotes)"
    String additionalParameter = "(?:\\s*$parameterName\\s*=\\s*$parameterValue\\s*,?\\s*)"
    String groupParameter = "group\\s*=\\s*\"$group\"\\s*,?\\s*"
    String nameParameter = "name\\s*=\\s*\"$name\"\\s*,?\\s*"
    List<String> oldModuleVersionKotlinSeparateNamedParametersMatchString() {
        String versionParameterValue = '\")[^\"]*(\"'
        String versionParameter = "version\\s*=\\s*$versionParameterValue\\s*,?\\s*"

        Closure<String> createPermutation = { String permutation ->
            '(\\(' +
                "\\s*$additionalParameter*" +
                permutation +
                "$additionalParameter*" +
            '\\))'
        }

        List<GString> permutations = [
                "$groupParameter$nameParameter$versionParameter",
                "$groupParameter$versionParameter$nameParameter",
                "$nameParameter$groupParameter$versionParameter",
                "$nameParameter$versionParameter$groupParameter",
                "$versionParameter$groupParameter$nameParameter",
                "$versionParameter$nameParameter$groupParameter",
        ]
        permutations.collect {
            createPermutation(it)
        }
    }
    String newVersionString() {
        '$1' + this.newVersion + '$2'
    }
    String variableUseStringFormatInterpolationMatchString() {
        // Capture variables of type $var and ${var}
        "[\"']" + this.group + ':' + this.name + ":[\$]\\{?([^{}].*?)\\}?[\"']"
    }
    String variableUseStringFormatPlusMatchString() {
        "[\"']" + this.group + ':' + this.name +
                ":[\"'][ \\t]*\\+[ \\t]*([a-zA-Z\$_][a-zA-Z\$_0-9\\.]*)[^a-zA-Z\$_0-9\\.]"
    }
    String variableUseMapFormatMatchString() {
        "group[ \\t]*:[ \\t]*[\"']" + this.group + "[\"'][ \\t]*,[ \\t]*name[ \\t]*:[ \\t]*[\"']" + this.name +
                "[\"'][ \\t]*,[ \\t]*version[ \\t]*:[ \\t]*([^\\s\"']+?)[\\s)]"
    }
    String variableInDependencySetString() {
        "dependencySet\\s*\\(\\s*group\\s*:\\s*[\"']" + this.group +
                "[\"']\\s*,\\s*version\\s*:\\s*([^\\s\"']+?)[\\s)]\\s*\\{[^}]*entry\\s*[\"']" + this.name + "[\"']"
    }
    String variableKotlinUnnamedParametersMatchString() {
        '(?:testRuntimeOnly|implementation|annotationProcessor|api|apiDependenciesMetadata|apiElements|compile|' +
                'compileClasspath|compileOnly|compileOnlyDependenciesMetadata|implementation|' +
                'implementationDependenciesMetadata|runtime|runtimeClasspath|runtimeElements|runtimeOnly|' +
                'runtimeOnlyDependenciesMetadata|testAnnotationProcessor|testApi|testApiDependenciesMetadata|' +
                'testCompile|testCompileClasspath|testCompileOnly|testCompileOnlyDependenciesMetadata|' +
                'testImplementation|testImplementationDependenciesMetadata|testKotlinScriptDef|' +
                'testKotlinScriptDefExtensions|testRuntime|testRuntimeClasspath|testRuntimeOnlyDependenciesMetadata)' +
                "\\s*\\(\\s*\"${this.group}\"\\s*,\\s*\"${this.name}\"\\s*,\\s*([^\\s\"']+)\\s*\\)"
    }
    List<String> variableKotlinSeparateNamedParametersMatchString() {
        String versionParameterValue = "($parameterValueWithoutQuotes)"
        String versionParameter = "version\\s*=\\s*$versionParameterValue\\s*,?\\s*"

        Closure<String> createPermutation = { String permutation ->
            '\\(' +
                "\\s*$additionalParameter*" +
                permutation +
                "$additionalParameter*" +
            '\\)'
        }

        List<GString> permutations = [
                "$groupParameter$nameParameter$versionParameter",
                "$groupParameter$versionParameter$nameParameter",
                "$nameParameter$groupParameter$versionParameter",
                "$nameParameter$versionParameter$groupParameter",
                "$versionParameter$groupParameter$nameParameter",
                "$versionParameter$nameParameter$groupParameter",
        ]
        permutations.collect {
            createPermutation(it)
        }
    }
    String toString() {
        "${this.group}:${this.name} [${this.oldVersion} -> ${this.newVersion}]"
    }
    String groupAndName() {
        "${this.group}:${this.name}"
    }
}
