package com.github.swagger.docgen.gradleplugin

import com.github.swagger.docgen.AbstractDocumentSource
import com.github.swagger.docgen.GenerateException
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.config.SwaggerConfig
import com.wordnik.swagger.core.SwaggerSpec
import com.wordnik.swagger.jaxrs.JaxrsApiReader
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader
import com.wordnik.swagger.model.ApiListing
import com.wordnik.swagger.model.ApiListingReference
import com.wordnik.swagger.model.AuthorizationType
import com.wordnik.swagger.model.ResourceListing
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.None
import scala.Option
import scala.collection.JavaConversions
import scala.collection.mutable.Buffer

/**
 * GradleDocumentSource 
 */
class GradleDocumentSource extends AbstractDocumentSource {
    private static final Logger logger = LoggerFactory.getLogger(GradleDocumentSource)

    private final SwaggerPluginExtension swagger
    private final ClassLoader classLoader

    public GradleDocumentSource(SwaggerPluginExtension swagger, ClassLoader classLoader) {
        super(swagger.outputPath, swagger.outputTemplate, swagger.swaggerDirectory, swagger.mustacheFileRoot,
                swagger.useOutputFlatStructure);

        this.swagger = swagger;
        this.classLoader = classLoader;

        this.setApiVersion(swagger.apiVersion);
        this.setBasePath(swagger.basePath);
    }

    @Override
    public void loadDocuments() throws GenerateException {
        logger.info("Loading documents using config: {}", swagger)

        SwaggerConfig swaggerConfig = new SwaggerConfig();
        swaggerConfig.setApiVersion(swagger.apiVersion);
        swaggerConfig.setSwaggerVersion(SwaggerSpec.version());
        List<ApiListingReference> apiListingReferences = new ArrayList<ApiListingReference>();
        List<AuthorizationType> authorizationTypes = new ArrayList<AuthorizationType>();
        for (Class c : getValidClasses()) {
            ApiListing doc;
            try {
                doc  = getDocFromClass(c, swaggerConfig, getBasePath());
            } catch (Exception e) {
                throw new GenerateException(e);
            }
            if (doc == null) continue;
            logger.info("Detect Resource:" + c.getName());

            Buffer<AuthorizationType> buffer = doc.authorizations().toBuffer();
            authorizationTypes.addAll(JavaConversions.asJavaList(buffer));
            ApiListingReference apiListingReference = new ApiListingReference(doc.resourcePath(), doc.description(), doc.position());
            apiListingReferences.add(apiListingReference);
            acceptDocument(doc);
        }
        // sort apiListingReference by position
        Collections.sort(apiListingReferences, new Comparator<ApiListingReference>() {
            @Override
            public int compare(ApiListingReference o1, ApiListingReference o2) {
                if (o1 == null && o2 == null) return 0;
                if (o1 == null && o2 != null) return -1;
                if (o1 != null && o2 == null) return 1;
                return  o1.position() - o2.position();
            }
        });
        serviceDocument = new ResourceListing(swaggerConfig.apiVersion(), swaggerConfig.swaggerVersion(),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiListingReferences.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizationTypes.iterator())),
                swaggerConfig.info());
    }

    private Set<Class> getValidClasses() throws GenerateException {

        Set<Class> classes = new HashSet<Class>();
        if (swagger.endPoints == null) {
            Set<Class<?>> c = new Reflections("").getTypesAnnotatedWith(Api.class);
            classes.addAll(c);
        } else {
            for (String endPoint : swagger.endPoints) {
                logger.info "Looking for valid classes in package: {}", endPoint

                Reflections reflections = new Reflections(ConfigurationBuilder.build(classLoader, endPoint))
                Set<Class<?>> c = reflections.getTypesAnnotatedWith(Api.class);

                classes.addAll(c);
            }
        }
        Iterator<Class> it = classes.iterator();
        while (it.hasNext()) {
            if (it.next().getName().startsWith("com.wordnik.swagger")) {
                it.remove();
            }
        }

        logger.info("Found valid classes: {}", classes)

        return classes;
    }

    private ApiListing getDocFromClass(Class c, SwaggerConfig swaggerConfig, String basePath) throws Exception {
        Api resource = (Api) c.getAnnotation(Api.class);

        if (resource == null) return null;
        JaxrsApiReader reader = new DefaultJaxrsApiReader();
        Option<ApiListing> apiListing = reader.read(basePath, c, swaggerConfig);

        if (None.canEqual(apiListing)) return null;

        return apiListing.get();
    }
}
