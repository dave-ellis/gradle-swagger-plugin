gradle-swagger-plugin
=====================

This a port of kongchen's maven plugin for generating swagger documents.  (See https://github.com/kongchen/swagger-maven-plugin)

# Usage

```
    buildscript {
        repositories {
            mavenLocal()
            maven { url "http://repo.maven.apache.org/maven2" }
        }
        dependencies {
            classpath group: 'com.github.gradle-swagger', name: 'gradle-swagger-plugin', version: '1.0.1-SNAPSHOT'
        }
    }

    apply plugin: 'maven'
    apply plugin: 'swagger'
    apply plugin: 'java'

    swagger {
        endPoints = [
                'com.foo.bar.apis',
                'com.foo.bar.apis.internal.Resource'
        ]
        apiVersion = 'v1'
        basePath = 'http://www.example.com'
        mustacheFileRoot = "${projectDir}/src/main/resources/"
        outputTemplate = "${mustacheFileRoot}/strapdown.html.mustache"
        swaggerDirectory = "${buildDir}/site/api-docs"
        outputPath = "${buildDir}/site/swagger/strapdown.html"
    }
```


* The swagger block currently only allows the definition of one `apiSource`.
* Java classes containing Swagger's annotation `@Api`, or Java packages containing those classes can be configured in `endPoints` list.
* `outputTemplate` is the path of the mustache template file.
* `outputPath` is the path of your output file, not existed parent directory of the file will be created.
* If `swaggerDirectory` is configured, the plugin will also generate a Swagger resource listing suitable for feeding to swagger-ui.
    * `useOutputFlatStructure` indicates whether swagger output will be created in sub-directories by path defined in
      `@com.wordnik.swagger.annotations.Api#value` (false), or the filename will be the path with replaced slashes to
      underscores (true). Default: true
