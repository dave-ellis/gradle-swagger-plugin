package com.github.swagger.docgen.gradleplugin;

import com.github.swagger.docgen.GenerateException;

/**
 * SwaggerDocumentGenerator
 */
class SwaggerDocumentGenerator {

    ClassLoader classLoader;

    public SwaggerDocumentGenerator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void generateSwaggerDocuments(SwaggerPluginExtension swagger) throws GenerateException {
        GradleDocumentSource documentSource = new GradleDocumentSource(swagger, classLoader);

        documentSource.loadDocuments();
        documentSource.toDocuments();
        documentSource.toSwaggerDocuments(swagger.getSwaggerUIDocBasePath());
    }
}
