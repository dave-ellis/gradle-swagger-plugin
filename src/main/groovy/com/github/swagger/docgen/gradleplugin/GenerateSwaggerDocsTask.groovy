package com.github.swagger.docgen.gradleplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * GradleSwaggerTask 
 */
class GenerateSwaggerDocsTask extends DefaultTask {

    @TaskAction
    def generateSwaggerDocs() {
        SwaggerPluginExtension swagger = project.swagger
        Iterable dependencies = project.configurations.runtime.resolve()
        File classesDir = project.sourceSets.main.output.classesDir

        project.logger.info "Swagger outputPath=${swagger.outputPath}, outputTemplate=${swagger.outputTemplate}"
        ClassLoader classLoader = prepareClassLoader(dependencies, classesDir)
        new SwaggerDocumentGenerator(classLoader)
                .generateSwaggerDocuments(swagger)
    }

    URLClassLoader prepareClassLoader(Iterable<File> dependencies, File dir) {
        List<URL> urls = dependencies.collect { it.toURI().toURL() }
        urls.add(dir.toURI().toURL())

        logger.debug "Preparing classloader with urls: {}", urls

        return new URLClassLoader(urls as URL[])
    }
}
