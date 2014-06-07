package com.github.swagger.docgen.gradleplugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.*

/**
 * GradleSwaggerTaskTest 
 */
class GenerateSwaggerDocsTaskTest {
    @Test
    void canAddTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('generateSwaggerDocs', type: GenerateSwaggerDocsTask)
        assertTrue(task instanceof GenerateSwaggerDocsTask)
    }
}
