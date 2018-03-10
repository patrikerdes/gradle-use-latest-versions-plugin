package se.patrikerdes

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import static org.junit.Assert.*

class UseLatestVersionsPluginTest {
    @Test
    void greeterPluginAddsGreetingTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'se.patrikerdes.use-latest-versions'
        assertTrue(project.tasks.useLatestVersions instanceof UseLatestVersionsTask)
    }
}
