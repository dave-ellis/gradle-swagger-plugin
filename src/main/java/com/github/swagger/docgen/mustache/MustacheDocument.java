package com.github.swagger.docgen.mustache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.swagger.docgen.util.Utils;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.util.ModelUtil;
import com.wordnik.swagger.model.*;
import scala.Option;
import scala.collection.Iterator;
import scala.collection.JavaConversions;
import scala.collection.immutable.*;
import scala.collection.mutable.LinkedEntry;

import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.wordnik.swagger.core.util.ModelUtil.modelAndDependencies;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 */
public class MustacheDocument implements Comparable<MustacheDocument> {
    private static final Logger logger = LoggerFactory.getLogger(MustacheDocument.class);


    protected static final String VOID = "void";

    protected static final String ARRAY = "Array";

    private static final String LIST = "List";

    @JsonIgnore
    private final Map<String, Model> models = new HashMap<>();

    private int index;

    private String resourcePath;

    private String description;

    private List<MustacheApi> apis = new ArrayList<>();

    @JsonIgnore
    private Set<String> requestTypes = new LinkedHashSet<>();

    @JsonIgnore
    private Set<String> responseTypes = new LinkedHashSet<>();

    @JsonIgnore
    private int apiIndex = 1;

    public MustacheDocument(ApiListing apiListing) {
        if (!apiListing.models().isEmpty()) {
            addModels(JavaConversions.mapAsJavaMap(apiListing.models().get()));
        }
        this.resourcePath = apiListing.resourcePath();
        this.index = apiListing.position();
        this.apis = new ArrayList<>(apiListing.apis().size());
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getDescription() {
        return description;
    }

    public List<MustacheApi> getApis() {
        return apis;
    }

    public Set<String> getRequestTypes() {
        return requestTypes;
    }

    public Set<String> getResponseTypes() {
        return responseTypes;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public void addApi(MustacheApi wapi) {
        apis.add(wapi);
    }

    public void addResponseType(MustacheResponseClass clz) {
        String className = clz.getClassName();
        if (className == null) {
            return;
        }

        String newName = addModels(JavaConversions.mapAsJavaMap(modelAndDependencies(className)));
        if (newName == null) {
            responseTypes.add(clz.getClassLinkName());
            return;
        }
        if (newName.equals(clz.getClassLinkName())) {
            responseTypes.add(newName);
        }

    }

    public List<MustacheParameterSet> analyzeParameters(List<Parameter> parameters) {
        if (parameters == null) return null;
        List<MustacheParameterSet> list = new LinkedList<>();

        Map<String, List<MustacheParameter>> paraMap = toParameterTypeMap(parameters);

        for (Map.Entry<String, List<MustacheParameter>> entry : paraMap.entrySet()) {
            list.add(new MustacheParameterSet(entry));
        }

        return list;
    }

    private Map<String, List<MustacheParameter>> toParameterTypeMap(List<Parameter> parameters) {
        Map<String, List<MustacheParameter>> paraMap = new HashMap<>();

        for (Parameter para : parameters) {
            MustacheParameter mustacheParameter = analyzeParameter(para);

            List<MustacheParameter> paraList = paraMap.get(para.paramType());
            if (paraList == null) {
                paraList = new LinkedList<>();
                paraMap.put(para.paramType(), paraList);
            }

            paraList.add(mustacheParameter);
        }
        return paraMap;
    }

    private MustacheParameter analyzeParameter(Parameter para) {
        MustacheParameter mustacheParameter = new MustacheParameter(para);

        if (models.get(mustacheParameter.getLinkType()) == null) {
            mustacheParameter.setName(para.name());
        } else {
            if (mustacheParameter.getLinkType() != null
                    && !para.paramType().equals(ApiValues.TYPE_HEADER())) {
                requestTypes.add(mustacheParameter.getLinkType());
            }
            if (para.name() != null) {
                mustacheParameter.setName(para.name());
            } else {
                mustacheParameter.setName(para.dataType());
            }
        }

        return mustacheParameter;
    }

    public List<MustacheItem> analyzeDataTypes(String responseClass) {
        List<MustacheItem> mustacheItemList = new ArrayList<>();
        if (responseClass == null || responseClass.equals(VOID)) {
            return mustacheItemList;
        }

        Model field = models.get(responseClass);
        logger.debug("Analyzing data type: {} -> {}", responseClass, field);

        if (field != null && field.properties() != null) {

            for (Iterator<LinkedEntry<String, ModelProperty>> it = field.properties().entriesIterator(); it.hasNext(); ) {
                LinkedEntry<String, ModelProperty> entry = it.next();
                MustacheItem mustacheItem = new MustacheItem(entry.key(), entry.value());

                Option<ModelRef> itemOption = entry.value().items();
                ModelRef item = itemOption.isEmpty() ? null : itemOption.get();

                if (mustacheItem.getType().equalsIgnoreCase(ARRAY)
                        || mustacheItem.getType().equalsIgnoreCase(LIST)) {
                    handleArrayType(mustacheItem, item);
                } else if (models.get(mustacheItem.getType())!= null) {
                    responseTypes.add(mustacheItem.getType());
                }

                mustacheItemList.add(mustacheItem);
            }
        }
        return mustacheItemList;
    }

    private void handleArrayType(MustacheItem mustacheItem, ModelRef item) {
        if (item != null) {
            if (item.type() == null && item.ref() != null) {
                mustacheItem.setTypeAsArray(Utils.getStrInOption(item.ref()));
                responseTypes.add(Utils.getStrInOption(item.ref()));
            } else {
                mustacheItem.setTypeAsArray(item.type());
            }
        }
    }

    @Override
    public int compareTo(MustacheDocument o) {
        if (o == null) {
            return 1;
        }
        return this.getResourcePath().compareTo(o.getResourcePath());
    }

    public String addModels(Map<String, Model> modelMap) {
        logger.debug("Adding models: {}", modelMap);

        if (modelMap == null || modelMap.isEmpty()) {
            return null;
        }
        models.putAll(modelMap);

        return modelMap.keySet().iterator().next();
    }
}

