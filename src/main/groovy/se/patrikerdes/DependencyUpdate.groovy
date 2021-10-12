package se.patrikerdes

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import java.util.regex.Pattern

@CompileStatic
@TupleConstructor
class DependencyUpdate {
    String group
    String groupQ
    String name
    String nameQ
    String oldVersion
    String oldVersionQ
    String newVersion

    DependencyUpdate(String group, String name, String oldVersion, String newVersion) {
        this.group = group
        this.groupQ = Pattern.quote(group)
        this.name = name
        this.nameQ = Pattern.quote(name)
        this.oldVersion = oldVersion
        this.oldVersionQ = Pattern.quote(oldVersion)
        this.newVersion = newVersion
    }
    String oldModuleVersionStringFormatMatchString() {
        "([\"']" + this.groupQ + ':' + this.nameQ + ":)[^\$].*?((:.*?)?[\"'])"
    }
    String oldModuleVersionMapFormatMatchString() {
        "(group[ \\t]*:[ \\t]*[\"']" + this.groupQ + "[\"'][ \\t]*,[ \\t]*name[ \\t]*:[ \\t]*[\"']" + this.nameQ +
                "[\"'][ \\t]*,[ \\t]*version[ \\t]*:[ \\t]*[\"']).*?([\"'])"
    }
    String oldModuleVersionDependencySetString() {
        "(dependencySet\\s*\\(\\s*group\\s*:\\s*[\"']" + this.groupQ +
                "[\"']\\s*,\\s*version\\s*:\\s*[\"'])[^\\s\"']+?([\"'][\\s)]\\s*\\{[^}]*entry\\s*[\"']" +
                this.nameQ + "[\"'])"
    }
    String oldPluginVersionMatchString() {
        "(id[ \\t\\(]+[\"']" + this.groupQ + "[\"'][ \\t\\)]+version[ \\t]+[\"'])" + this.oldVersionQ + "([\"'])"
    }
    String oldKotlinPluginVersionMatchString() {
        '(kotlin\\(\"' +
                (this.group.startsWith('org.jetbrains.kotlin.') ? this.group - 'org.jetbrains.kotlin.' : this.group) +
                '\"\\)\\s+version\\s+\")' + this.oldVersionQ + '(\")'
    }
    String oldModuleVersionKotlinUnnamedParametersMatchString() {
        '((?:testRuntimeOnly|implementation|annotationProcessor|api|apiDependenciesMetadata|apiElements|compile|' +
                'compileClasspath|compileOnly|compileOnlyDependenciesMetadata|implementation|' +
                'implementationDependenciesMetadata|runtime|runtimeClasspath|runtimeElements|runtimeOnly|' +
                'runtimeOnlyDependenciesMetadata|testAnnotationProcessor|testApi|testApiDependenciesMetadata|' +
                'testCompile|testCompileClasspath|testCompileOnly|testCompileOnlyDependenciesMetadata|' +
                'testImplementation|testImplementationDependenciesMetadata|testKotlinScriptDef|' +
                'testKotlinScriptDefExtensions|testRuntime|testRuntimeClasspath|testRuntimeOnlyDependenciesMetadata)' +
                "\\s*\\(\\s*\"${this.groupQ}\"\\s*,\\s*\"${this.nameQ}\"\\s*,\\s*\")${this.oldVersionQ}(\"\\s*\\))"
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
        "[\"']" + this.groupQ + ':' + this.nameQ + ":[\$]\\{?([^{}].*?)\\}?[\"']"
    }
    String variableUseStringFormatPlusMatchString() {
        "[\"']" + this.groupQ + ':' + this.nameQ +
                ":[\"'][ \\t]*\\+[ \\t]*([a-zA-Z\$_][a-zA-Z\$_0-9\\.]*)[^a-zA-Z\$_0-9\\.]"
    }
    String variableUseMapFormatMatchString() {
        "group[ \\t]*:[ \\t]*[\"']" + this.groupQ + "[\"'][ \\t]*,[ \\t]*name[ \\t]*:[ \\t]*[\"']" + this.nameQ +
                "[\"'][ \\t]*,[ \\t]*version[ \\t]*:[ \\t]*([^\\s\"']+?)[\\s)]"
    }
    String variableInDependencySetString() {
        "dependencySet\\s*\\(\\s*group\\s*:\\s*[\"']" + this.groupQ +
                "[\"']\\s*,\\s*version\\s*:\\s*([^\\s\"']+?)[\\s)]\\s*\\{[^}]*entry\\s*[\"']" + this.nameQ + "[\"']"
    }
    String variableKotlinUnnamedParametersMatchString() {
        '(?:testRuntimeOnly|implementation|annotationProcessor|api|apiDependenciesMetadata|apiElements|compile|' +
                'compileClasspath|compileOnly|compileOnlyDependenciesMetadata|implementation|' +
                'implementationDependenciesMetadata|runtime|runtimeClasspath|runtimeElements|runtimeOnly|' +
                'runtimeOnlyDependenciesMetadata|testAnnotationProcessor|testApi|testApiDependenciesMetadata|' +
                'testCompile|testCompileClasspath|testCompileOnly|testCompileOnlyDependenciesMetadata|' +
                'testImplementation|testImplementationDependenciesMetadata|testKotlinScriptDef|' +
                'testKotlinScriptDefExtensions|testRuntime|testRuntimeClasspath|testRuntimeOnlyDependenciesMetadata)' +
                "\\s*\\(\\s*\"${this.groupQ}\"\\s*,\\s*\"${this.nameQ}\"\\s*,\\s*([^\\s\"']+)\\s*\\)"
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
