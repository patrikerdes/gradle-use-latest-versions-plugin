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
    DependencyUpdate(group, name, oldVersion, newVersion) {
        this.group = group
        this.name = name
        this.oldVersion = oldVersion
        this.newVersion = newVersion
    }
    String oldModuleVersionStringFormatMatchString() {
        return "([\"']" + this.group + ":" + this.name + ":)[^\$].*?([\"'])"
    }
    String oldModuleVersionMapFormatMatchString() {
        return "(group[ \\t]*:[ \\t]*[\"']" + this.group + "[\"'][ \\t]*,[ \\t]*name[ \\t]*:[ \\t]*[\"']" + this.name + "[\"'][ \\t]*,[ \\t]*version[ \\t]*:[ \\t]*[\"']).*?([\"'])"
    }
    String oldPluginVersionMatchString() {
        return "(id[ \\t]+[\"']" + this.group + "[\"'][ \\t]+version[ \\t]+[\"'])" + this.oldVersion + "([\"'])"
    }
    String newVersionString() {
        return '$1' + this.newVersion + '$2'
    }
    String variableUseStringFormatMatchString() {
        // Capture variables starting with $, but not expresions starting with ${
        return "[\"']" + this.group + ":" + this.name + ":[\$]([^{].*?)[\"']"
    }
    String variableUseMapFormatMatchString() {
        return "group[ \\t]*:[ \\t]*[\"']" + this.group + "[\"'][ \\t]*,[ \\t]*name[ \\t]*:[ \\t]*[\"']" + this.name + "[\"'][ \\t]*,[ \\t]*version[ \\t]*:[ \\t]*([^\\s]+?)\\s"
    }
}
