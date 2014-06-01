package com.github.swagger.docgen.gradleplugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * SwaggerDocumentGeneratorTest
 */
public class SwaggerDocumentGeneratorTest {

    SwaggerDocumentGenerator generator = new SwaggerDocumentGenerator(this.getClass().getClassLoader());

    String tmpSwaggerOutputDir = "apidocsf";

    SwaggerPluginExtension swagger;

    @BeforeMethod
    private void prepare() throws MalformedURLException {
        swagger = new SwaggerPluginExtension();
        swagger.setApiVersion("1.0");
        swagger.setBasePath("http://example.com");
        swagger.setSwaggerUIDocBasePath("http://localhost/apidocsf");
        swagger.setEndPoints(new String[]{"sample.api"});
        swagger.setOutputPath("temp.html");
        swagger.setOutputTemplate("src/test/resoures/strapdown.html.mustache");
        swagger.setSwaggerDirectory(tmpSwaggerOutputDir);
        swagger.setUseOutputFlatStructure(false);
    }

    @AfterMethod
    private void fin() throws IOException {
        File tempOutput = new File(tmpSwaggerOutputDir);
//        FileUtils.deleteDirectory(tempOutput);
    }

    /**
     * {
     * "apiVersion" : "1.0",
     * "swaggerVersion" : "1.1",
     * "basePath" : "http://localhost/apidocsf",
     * "apis" : [ {
     * "path" : "/v2_car.{format}",
     * "description" : "Operations about cars"
     * }, {
     * "path" : "/garage.{format}",
     * "description" : "Operations about garages"
     * }, {
     * "path" : "/car.{format}",
     * "description" : "Operations about cars"
     * } ]
     * }
     */
    @Test
    public void testSwaggerOutputFlat() throws Exception {
        swagger.setSwaggerDirectory(tmpSwaggerOutputDir);
        swagger.setUseOutputFlatStructure(true);

        File output = new File(tmpSwaggerOutputDir);
        FileUtils.deleteDirectory(output);

        generator.generateSwaggerDocuments(swagger);
        List<String> flatfiles = new ArrayList<String>();

        Collections.addAll(flatfiles, output.list());
        Collections.sort(flatfiles);
        Assert.assertEquals(flatfiles.get(0), "car.json");
        Assert.assertEquals(flatfiles.get(1), "garage.json");
        Assert.assertEquals(flatfiles.get(2), "service.json");
        Assert.assertEquals(flatfiles.get(3), "v2_car.json");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(FileUtils.readFileToByteArray(new File(output, "service.json")));
        JsonNode apis = node.get("apis");
        Assert.assertEquals(apis.size(), 3);
        List<String> pathInService = new ArrayList<>();
        for (JsonNode api : apis) {
            pathInService.add(api.get("path").asText());
        }
        Collections.sort(pathInService);
        Assert.assertEquals(pathInService.get(0), "/car.{format}");
        Assert.assertEquals(pathInService.get(1), "/garage.{format}");
        Assert.assertEquals(pathInService.get(2), "/v2_car.{format}");
    }

    @Test
    public void testSwaggerOutput() throws Exception {
        swagger.setSwaggerDirectory(tmpSwaggerOutputDir);
        swagger.setUseOutputFlatStructure(false);

        File output = new File(tmpSwaggerOutputDir);
        FileUtils.deleteDirectory(output);

        generator.generateSwaggerDocuments(swagger);
        List<File> outputFiles = new ArrayList<>();

        Collections.addAll(outputFiles, output.listFiles());
        Collections.sort(outputFiles);
        Assert.assertEquals(outputFiles.get(0).getName(), "car.json");
        Assert.assertEquals(outputFiles.get(1).getName(), "garage.json");
        Assert.assertEquals(outputFiles.get(2).getName(), "service.json");
        Assert.assertEquals(outputFiles.get(3).getName(), "v2");
        File v2 = outputFiles.get(3);
        Assert.assertTrue(v2.isDirectory());
        String[] v2carfile = v2.list();
        Assert.assertEquals(v2carfile[0], "car.json");


        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(FileUtils.readFileToByteArray(new File(output, "service.json")));
        JsonNode apis = node.get("apis");
        Assert.assertEquals(apis.size(), 3);
        List<String> pathInService = new ArrayList<>();
        for (JsonNode api : apis) {
            pathInService.add(api.get("path").asText());
        }
        Collections.sort(pathInService);
        Assert.assertEquals(pathInService.get(0), "/car.{format}");
        Assert.assertEquals(pathInService.get(1), "/garage.{format}");
        Assert.assertEquals(pathInService.get(2), "/v2/car.{format}");
    }

    @Test(enabled = true)
    public void testExecute() throws Exception {
        generator.generateSwaggerDocuments(swagger);
        FileInputStream testOutputIs = new FileInputStream(new File("temp.html"));
        InputStream expectIs = this.getClass().getResourceAsStream("/sample.html");
        int count = 0;
        while (true) {
            count++;
            int expect = expectIs.read();
            int actual = testOutputIs.read();

            Assert.assertEquals( expect, actual, ""+count);
            if (expect == -1) {
                break;
            }
        }
    }

    @DataProvider
    private Iterator<String[]> pathProvider() throws Exception {
        String tempDirPath = createTempDirPath();

        List<String[]> dataToBeReturned = new ArrayList<>();
        dataToBeReturned.add(new String[]{tempDirPath + "foo" + File.separator + "bar" + File
                .separator + "test.html"});
        dataToBeReturned.add(new String[]{tempDirPath + File.separator + "bar" + File.separator +
                "test.html"});
        dataToBeReturned.add(new String[]{tempDirPath + File.separator + "test.html"});
        dataToBeReturned.add(new String[]{"test.html"});

        return dataToBeReturned.iterator();
    }

    @Test(dataProvider = "pathProvider")
    public void testExecuteDirectoryCreated(String path) throws Exception {

        swagger.setOutputPath(path);

        File file = new File(path);
        generator.generateSwaggerDocuments(swagger);
        Assert.assertTrue(file.exists());
        if (file.getParentFile() != null) {
            FileUtils.deleteDirectory(file.getParentFile());
        }
    }

    private String createTempDirPath() throws Exception {
        File tempFile = File.createTempFile("swagmvn", "test");
        String path = tempFile.getAbsolutePath();
        tempFile.delete();
        return path;
    }
}
