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
        "([\"']" + this.group + ':' + this.name + ":)[^\$].*?([\"'])"
    }
    String oldModuleVersionMapFormatMatchString() {
        "(group[ \\t]*:[ \\t]*[\"']" + this.group + "[\"'][ \\t]*,[ \\t]*name[ \\t]*:[ \\t]*[\"']" + this.name +
                "[\"'][ \\t]*,[ \\t]*version[ \\t]*:[ \\t]*[\"']).*?([\"'])"
    }
    String oldPluginVersionMatchString() {
        "(id[ \\t\\(]+[\"']" + this.group + "[\"'][ \\t\\)]+version[ \\t]+[\"'])" + this.oldVersion + "([\"'])"
    }
    String newVersionString() {
        '$1' + this.newVersion + '$2'
    }
    String variableUseStringFormatInterpolationMatchString() {
        // Capture variables starting with $, but not expresions starting with ${
        "[\"']" + this.group + ':' + this.name + ":[\$]([^{].*?)[\"']"
    }
    String variableUseStringFormatPlusMatchString() {
        "[\"']" + this.group + ':' + this.name +
                ":[\"'][ \\t]*\\+[ \\t]*([a-zA-Z\$_][a-zA-Z\$_0-9\\.]*)[^a-zA-Z\$_0-9\\.]"
    }
    String variableUseMapFormatMatchString() {
        "group[ \\t]*:[ \\t]*[\"']" + this.group + "[\"'][ \\t]*,[ \\t]*name[ \\t]*:[ \\t]*[\"']" + this.name +
                "[\"'][ \\t]*,[ \\t]*version[ \\t]*:[ \\t]*([^\\s\"']+?)[\\s)]"
    }
    String toString() {
        "${this.group}:${this.name} [${this.oldVersion} -> ${this.newVersion}]"
    }
    String groupAndName() {
        "${this.group}:${this.name}"
    }
}
