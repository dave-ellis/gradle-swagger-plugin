gradle-swagger-plugin
=====================

This a port of kongchen's maven plugin for generating swagger documents.

```
swagger {
    endPoints = [
            'com.recordsure.vaultserver.endpoints.recordings',
            'com.recordsure.vaultserver.endpoints.uploads',
            'com.recordsure.vaultserver.endpoints.base',
            'com.recordsure.vaultserver.endpoints.segments',
            'com.recordsure.vaultserver.endpoints.phrases'
    ]
    apiVersion = 'v1'
    basePath = 'http://host:port/vaultService'
    mustacheFileRoot = "${projectDir}/src/main/resources/"
    outputTemplate = "${mustacheFileRoot}/strapdown.html.mustache"
    swaggerDirectory = "${buildDir}/site/api-docs"
    outputPath = "${buildDir}/site/swagger/strapdown.html"
}
```