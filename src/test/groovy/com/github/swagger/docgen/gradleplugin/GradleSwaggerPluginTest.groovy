package com.github.swagger.docgen.gradleplugin

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import static org.junit.Assert.*

/**
 * GradleSwaggerPluginTest 
 */
class GradleSwaggerPluginTest {
    void setUp() {

    }

    @Test
    void pluginAddsGenerateSwaggerTask() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'swagger'
        assertTrue(project.tasks.generateSwaggerDocs instanceof GenerateSwaggerDocsTask)

    }
}
