package com.github.swagger.docgen.gradleplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * GradleSwaggerPlugin
 */
class GradleSwaggerPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("swagger", SwaggerPluginExtension)

        project.task([dependsOn:'classes'], 'generateSwaggerDocs') << {
            SwaggerPluginExtension swagger = project.swagger
            Iterable dependencies = project.configurations.runtime.resolve()
            File classesDir = project.sourceSets.main.output.classesDir

            project.logger.info "Swagger outputPath=${swagger.outputPath}, outputTemplate=${swagger.outputTemplate}"
            ClassLoader classLoader = prepareClassLoader(dependencies, classesDir)
            new SwaggerDocumentGenerator(classLoader)
                    .generateSwaggerDocuments(swagger)
        }
        project.build.dependsOn 'generateSwaggerDocs'
    }

    private URLClassLoader prepareClassLoader(Iterable<File> dependencies, File dir) {
        List<URL> urls = dependencies.collect { File file ->
            file.toURI().toURL()
        }

        URL url = dir.toURI().toURL()
        urls.add(url)

        return new URLClassLoader(urls as URL[])
    }
}

