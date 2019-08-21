/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.influxdb.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.InlineModelResolver;
import org.openapitools.codegen.languages.JavaClientCodegen;
import org.openapitools.codegen.utils.ModelUtils;

public class InfluxJavaGenerator extends JavaClientCodegen implements CodegenConfig {

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return "influx-java";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a influx-java client library.";
    }

    private OpenAPI openAPI;

    public InfluxJavaGenerator() {

        super();

        importMapping.put("JSON", "com.influxdb.client.JSON");
        importMapping.put("JsonDeserializer", "com.google.gson.JsonDeserializer");
        importMapping.put("JsonDeserializationContext", "com.google.gson.JsonDeserializationContext");
        importMapping.put("JsonSerializationContext", "com.google.gson.JsonSerializationContext");
        importMapping.put("JsonSerializer", "com.google.gson.JsonSerializer");
        importMapping.put("JsonParseException", "com.google.gson.JsonParseException");
        importMapping.put("ArrayList", "java.util.ArrayList");
        importMapping.put("List", "java.util.List");
        importMapping.put("JsonObject", "com.google.gson.JsonObject");
        importMapping.put("JsonArray", "com.google.gson.JsonArray");
        importMapping.put("JsonElement", "com.google.gson.JsonElement");
        importMapping.put("HashMap", "java.util.HashMap");
        importMapping.put("Map", "java.util.Map");
        importMapping.put("ReflectType", "java.lang.reflect.Type");

        //
        // File is mapped to schema not to java.io.File
        //
        importMapping.remove("File");

        setUseNullForUnknownEnumValue(true);
    }

    @Override
    public void setGlobalOpenAPI(final OpenAPI openAPI) {

        super.setGlobalOpenAPI(openAPI);

        InlineModelResolver inlineModelResolver = new InlineModelResolver();
        inlineModelResolver.flatten(openAPI);

        String[] schemaNames = openAPI.getComponents().getSchemas().keySet().toArray(new String[0]);
        for (String schemaName : schemaNames) {
            Schema schema = openAPI.getComponents().getSchemas().get(schemaName);
            if (schema instanceof ComposedSchema) {


                List<Schema> allOf = ((ComposedSchema) schema).getAllOf();
                if (allOf != null) {

                    allOf.forEach(child -> {

                        if (child instanceof ObjectSchema) {

                            inlineModelResolver.flattenProperties(child.getProperties(), schemaName);
                        }
                    });
                }
            }
        }
    }

    @Override
    public Map<String, Object> postProcessAllModels(final Map<String, Object> models) {

        //
        // Remove type selectors
        //
        Map<String, Object> allModels = super.postProcessAllModels(models);
        additionalProperties.remove("parent");

        for (Map.Entry<String, Object> entry : allModels.entrySet()) {

            String modelName = entry.getKey();
            Object modelConfig = entry.getValue();

            CodegenModel pluginModel = getModel((HashMap) modelConfig);

            //
            // Set Telegraf Plugin name and type in constructors
            //
            if (modelName.startsWith("TelegrafPlugin") && !modelName.endsWith("Request") && !modelName.toLowerCase().contains("config")) {

                CodegenProperty configProperty = getCodegenProperty(pluginModel, "config");
                CodegenProperty typeProperty = getCodegenProperty(pluginModel, "type");
                CodegenProperty nameProperty = getCodegenProperty(pluginModel, "name");

                // set generic type: name + config
                pluginModel.parent = "TelegrafRequestPlugin<" + pluginModel.name + ".NameEnum, " + (configProperty != null ? configProperty.baseType : "Void") + ">";

                // Set Name and Type in Constructor
                ArrayList<Object> constructorItems = new ArrayList<>();
                constructorItems.add(String.format("setName(%s);", pluginModel.name + ".NameEnum." + getEnumDefaultValue(pluginModel, "name")));
                constructorItems.add(String.format("setType(%s);", "TelegrafRequestPlugin.TypeEnum." + getEnumDefaultValue(pluginModel, "type")));

                pluginModel.vendorExtensions.put("x-has-constructor-items", Boolean.TRUE);
                pluginModel.vendorExtensions.put("x-constructor-items", constructorItems);

                pluginModel.vendorExtensions.put("x-has-inner-enums", Boolean.TRUE);
                pluginModel.vendorExtensions.put("x-inner-enums", Arrays.asList(nameProperty));

                pluginModel.vars.remove(configProperty);
                pluginModel.vars.remove(typeProperty);
                pluginModel.vars.remove(nameProperty);
                pluginModel.vars.get(pluginModel.vars.size() - 1).hasMore = false;
            }

            //
            // The "interfaces" extends base object
            //
            if (!pluginModel.hasVars && pluginModel.interfaces != null && !modelName.startsWith("Telegraf")) {

                for (String interfaceModelName : pluginModel.interfaces) {

                    CodegenModel interfaceModel = getModel((HashMap) models.get(interfaceModelName));
                    interfaceModel.setParent(pluginModel.classname);
                }

                pluginModel.interfaces.clear();
            }

            //
            //
            //
            if (modelName.equals("PropertyKey")) {

                pluginModel.setParent("Expression");
            }
        }

        return allModels;
    }

    @Override
    public void processOpts() {

        super.processOpts();

        //
        // We want to use only the JSON.java
        //
        supportingFiles = supportingFiles.stream()
                .filter(supportingFile -> supportingFile.destinationFilename.equals("JSON.java"))
                .collect(Collectors.toList());
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {

        super.postProcessModelProperty(model, property);

        //
        // If its a constant then set default value
        //
        if (property.isEnum && property.get_enum() != null && property.get_enum().size() == 1) {
            property.isReadOnly = true;
            property.defaultValue = property.enumName + "." + getEnumDefaultValue(model, property.name);
        }
    }

    @Override
    public Map<String, Object> postProcessOperationsWithModels(final Map<String, Object> objs,
                                                               final List<Object> allModels) {

        Map<String, Object> operationsWithModels = super.postProcessOperationsWithModels(objs, allModels);

        List<CodegenOperation> operations = (List<CodegenOperation>) ((HashMap) operationsWithModels.get("operations"))
                .get("operation");

        //
        // For operations with more response type generate additional implementation
        //
        List<CodegenOperation> operationToSplit = operations.stream()
                .filter(operation -> operation.produces.size() > 1)
                .collect(Collectors.toList());

        operationToSplit.forEach(operation -> {

            List<String> returnTypes = operation.produces.stream()
                    .filter(produce -> operation.produces.indexOf(produce) != 0)
                    .map(produce -> {

                        PathItem path = openAPI.getPaths().get(StringUtils.substringAfter(operation.path, "/v2"));

                        Operation apiOperation;
                        switch (operation.httpMethod.toLowerCase()) {

                            case "get":
                                apiOperation = path.getGet();
                                break;

                            case "post":
                                apiOperation = path.getPost();
                                break;

                            default:
                                throw new IllegalStateException();
                        }

                        Schema responseSchema = apiOperation.getResponses().get("200").getContent().get(produce.get("mediaType")).getSchema();

                        if (responseSchema.get$ref() != null) {

                            String modelName = ModelUtils.getSimpleRef(responseSchema.get$ref());

                            CodegenModel model = (CodegenModel) ((HashMap) allModels.stream()
                                    .filter(it -> modelName.equals(((CodegenModel) ((HashMap) it).get("model")).name))
                                    .findFirst()
                                    .get()).get("model");

                            return model.classname;
                        } else {
                            return camelize(responseSchema.getType());
                        }

                    })
                    .distinct()
                    .collect(Collectors.toList());

            if (!returnTypes.isEmpty()) {
                returnTypes.add("ResponseBody");
            }

            returnTypes.forEach(returnType -> {
                CodegenOperation codegenOperation = new CodegenOperation();
                codegenOperation.baseName = operation.baseName + returnType;
                codegenOperation.summary = operation.summary;
                codegenOperation.notes = operation.notes;
                codegenOperation.allParams = operation.allParams;
                codegenOperation.httpMethod = operation.httpMethod;
                codegenOperation.path = operation.path;
                codegenOperation.returnType = returnType;
                codegenOperation.operationId = operation.operationId + returnType;

                operations.add(operations.indexOf(operation) + 1, codegenOperation);
            });
        });

        //
        // For basic auth add authorization header
        //
        operations.stream()
                .filter(operation -> operation.hasAuthMethods)
                .forEach(operation -> {

                    operation.authMethods.stream()
                            .filter(security -> security.isBasic)
                            .forEach(security -> {

                                CodegenParameter authorization = new CodegenParameter();
                                authorization.isHeaderParam = true;
                                authorization.isPrimitiveType = true;
                                authorization.isString = true;
                                authorization.baseName = "Authorization";
                                authorization.paramName = "authorization";
                                authorization.dataType = "String";
                                authorization.description = "An auth credential for the Basic scheme";

                                operation.allParams.get(operation.allParams.size() - 1).hasMore = true;
                                operation.allParams.add(authorization);
                            });
                });

        return operationsWithModels;

    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Schema> definitions, OpenAPI openAPI) {

        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, openAPI);

        //
        // Set base path
        //
        String url;
        if (operation.getServers() != null) {
            url = operation.getServers().get(0).getUrl();
        } else if (openAPI.getPaths().get(path).getServers() != null) {
            url = openAPI.getPaths().get(path).getServers().get(0).getUrl();
        } else {
            url = openAPI.getServers().get(0).getUrl();
        }

        if (!url.equals("/")) {
            op.path = url + op.path;
        }

        return op;
    }


    @Override
    public CodegenModel fromModel(final String name, final Schema model, final Map<String, Schema> allDefinitions) {

        CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);
        codegenModel.setDiscriminator(null);
        
        if (model.getProperties() != null) {


            Map properties = model.getProperties();

            properties
                    .forEach((BiConsumer<String, Schema>) (property, propertySchema) -> {

                        Schema schema = propertySchema;

                        //
                        // Reference to List of Object
                        //
                        if (schema instanceof ArraySchema) {
                            String ref = ((ArraySchema) schema).getItems().get$ref();
                            if (ref != null) {
                                String refSchemaName = ModelUtils.getSimpleRef(ref);
                                Schema refSchema = allDefinitions.get(refSchemaName);

                                if (refSchema instanceof ComposedSchema) {
                                    if (((ComposedSchema) refSchema).getOneOf() != null) {
                                        schema = refSchema;
                                    }
                                }
                            }
                        }

                        //
                        // Reference to Object
                        //
                        else if (schema.get$ref() != null) {
                            String refSchemaName = ModelUtils.getSimpleRef(schema.get$ref());
                            Schema refSchema = allDefinitions.get(refSchemaName);

                            if (refSchema instanceof ComposedSchema) {
                                if (((ComposedSchema) refSchema).getOneOf() != null) {
                                    schema = refSchema;
                                }
                            }
                        }

                        if (schema instanceof ComposedSchema) {

                            CodegenProperty codegenProperty = getCodegenProperty(codegenModel, property);
                            String adapterName = name + codegenProperty.nameInCamelCase + "Adapter";

                            Map<String, TypeAdapter> adapters = (HashMap<String, TypeAdapter>) codegenModel.vendorExtensions
                                    .getOrDefault("x-type-adapters", new HashMap<String, TypeAdapter>());

                            TypeAdapter typeAdapter = new TypeAdapter();
                            typeAdapter.classname = adapterName;

                            for (Schema oneOf : getOneOf(schema, allDefinitions)) {

                                String refSchemaName;
                                Schema refSchema;

                                if (oneOf.get$ref() == null) {
                                    refSchema = oneOf;
                                    refSchemaName = oneOf.getName();
                                } else {
                                    refSchemaName = ModelUtils.getSimpleRef(oneOf.get$ref());
                                    refSchema = allDefinitions.get(refSchemaName);
                                    if (refSchema instanceof ComposedSchema) {
                                        List<Schema> schemaList = ((ComposedSchema) refSchema).getAllOf().stream()
                                                .map(it -> getObjectSchemas(it, allDefinitions))
                                                .flatMap(Collection::stream)
                                                .filter(it -> it instanceof ObjectSchema).collect(Collectors.toList());
                                        refSchema = schemaList
                                                .stream()
                                                .filter(it -> {
                                                    for (Schema ps : (Collection<Schema>) it.getProperties().values()) {
                                                        if (ps.getEnum() != null && ps.getEnum().size() == 1) {
                                                            return true;
                                                        }
                                                    }
                                                    return false;
                                                })
                                                .findFirst()
                                                .orElse(schemaList.get(0));
                                    }
                                }

                                String[] keys = getDiscriminatorKeys(schema, refSchema);

                                String[] discriminator = new String[]{};
                                String[] discriminatorValue = new String[]{};

                                for (String key : keys) {
                                    Schema keyScheme = (Schema) refSchema.getProperties().get(key);
                                    if (keyScheme.get$ref() != null) {
                                        keyScheme = allDefinitions.get(ModelUtils.getSimpleRef(keyScheme.get$ref()));
                                    }

                                    if (!(keyScheme instanceof StringSchema)) {
                                        continue;
                                    } else {

                                        if (((StringSchema) keyScheme).getEnum() != null) {
                                            discriminatorValue = ArrayUtils.add(discriminatorValue, ((StringSchema) keyScheme).getEnum().get(0));
                                        } else {
                                            discriminatorValue = ArrayUtils.add(discriminatorValue, refSchemaName);
                                        }
                                    }

                                    discriminator = ArrayUtils.add(discriminator, key);
                                }

                                typeAdapter.isArray = propertySchema instanceof ArraySchema;
                                typeAdapter.discriminator = Stream.of(discriminator).map(v -> "\"" + v + "\"").collect(Collectors.joining(", "));
                                TypeAdapterItem typeAdapterItem = new TypeAdapterItem();
                                typeAdapterItem.discriminatorValue = Stream.of(discriminatorValue).map(v -> "\"" + v + "\"").collect(Collectors.joining(", "));
                                typeAdapterItem.classname = refSchemaName;
                                typeAdapter.items.add(typeAdapterItem);
                            }

                            if (!typeAdapter.items.isEmpty()) {

                                codegenProperty.vendorExtensions.put("x-has-type-adapter", Boolean.TRUE);
                                codegenProperty.vendorExtensions.put("x-type-adapter", adapterName);

                                adapters.put(adapterName, typeAdapter);

                                codegenModel.vendorExtensions.put("x-type-adapters", adapters);
                                codegenModel.imports.add("JsonDeserializer");
                                codegenModel.imports.add("JsonDeserializationContext");
                                codegenModel.imports.add("JsonSerializer");
                                codegenModel.imports.add("JsonSerializationContext");
                                codegenModel.imports.add("ArrayList");
                                codegenModel.imports.add("List");
                                codegenModel.imports.add("JsonElement");
                                codegenModel.imports.add("JsonObject");
                                codegenModel.imports.add("JsonArray");
                                codegenModel.imports.add("JsonParseException");
                                codegenModel.imports.add("ReflectType");
                            }
                        }
                    });
        }

        //
        // Add generic name, type and config property
        //
        if (name.equals("TelegrafRequestPlugin")) {

            codegenModel.interfaces.clear();

            //
            // Add generic name
            //
            CodegenProperty nameProperty = new CodegenProperty();
            nameProperty.name = "name";
            nameProperty.baseName = "name";
            nameProperty.getter = "getName";
            nameProperty.setter = "setName";
            nameProperty.dataType = "T";
            nameProperty.datatypeWithEnum = "T";
            nameProperty.nameInSnakeCase = "NAME";
            nameProperty.isReadOnly = false;
            nameProperty.hasMore = true;
            nameProperty.hasMoreNonReadOnly = true;
            postProcessModelProperty(codegenModel, nameProperty);
            codegenModel.vars.add(nameProperty);
            codegenModel.readWriteVars.add(nameProperty);

            //
            // Add type
            //
            CodegenProperty typeProperty = new CodegenProperty();
            typeProperty.name = "type";
            typeProperty.baseName = "type";
            typeProperty.getter = "getType";
            typeProperty.setter = "setType";
            typeProperty.dataType = "String";
            typeProperty.isEnum = true;
            typeProperty.set_enum(Arrays.asList("input", "output"));

            final HashMap<String, Object> allowableValues = new HashMap<>();

            List<Map<String, String>> enumVars = new ArrayList<>();
            for (String value : Arrays.asList("input", "output")) {
                Map<String, String> enumVar = new HashMap<>();
                enumVar.put("name", value.toUpperCase());
                enumVar.put("value", "\"" + value + "\"");
                enumVars.add(enumVar);
            }
            allowableValues.put("enumVars", enumVars);

            typeProperty.setAllowableValues(allowableValues);
            typeProperty.datatypeWithEnum = "TypeEnum";
            typeProperty.nameInSnakeCase = "TYPE";
            typeProperty.isReadOnly = false;
            typeProperty.hasMore = true;
            typeProperty.hasMoreNonReadOnly = true;
            postProcessModelProperty(codegenModel, typeProperty);
            codegenModel.vars.add(typeProperty);
            codegenModel.readWriteVars.add(typeProperty);

            //
            // Add generic config
            //
            CodegenProperty configProperty = new CodegenProperty();
            configProperty.name = "config";
            configProperty.baseName = "config";
            configProperty.getter = "getConfig";
            configProperty.setter = "setConfig";
            configProperty.dataType = "C";
            configProperty.datatypeWithEnum = "C";
            configProperty.nameInSnakeCase = "CONFIG";
            configProperty.isReadOnly = false;
            postProcessModelProperty(codegenModel, configProperty);
            codegenModel.vars.add(configProperty);
            codegenModel.readWriteVars.add(configProperty);

            //
            // Add generics to class
            //
            codegenModel.vendorExtensions.put("x-has-generic-type", Boolean.TRUE);
            codegenModel.vendorExtensions.put("x-generic-type", "<T, C>");
        }

        return codegenModel;
    }

    @Override
    public String toApiName(String name) {

        if (name.length() == 0) {
            return super.toApiName(name);
        }

        //
        // Rename "Api" to "Service"
        //
        return camelize(name) + "Service";
    }

    @Override
    public void preprocessOpenAPI(final OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        this.openAPI = openAPI;
    }

    @Nonnull
    private String getEnumValue(final CodegenModel model, final String propertyName) {

        CodegenProperty codegenProperty = getCodegenProperty(model, propertyName);
        if (codegenProperty == null) {
            throw new IllegalStateException("Model: " + model + " doesn't have a property: " + propertyName);
        }

        return codegenProperty.get_enum().get(0);
    }

    @Nonnull
    private String getEnumDefaultValue(final CodegenModel model, final String propertyName) {

        String enumValue = getEnumValue(model, propertyName);

        return enumValue.toUpperCase().replace("-", "_");
    }

    @Nullable
    private CodegenProperty getCodegenProperty(final CodegenModel model, final String propertyName) {
        return model.vars.stream()
                .filter(property -> property.name.equals(propertyName))
                .findFirst().orElse(null);
    }

    @Nonnull
    private CodegenModel getModel(@Nonnull final HashMap modelConfig) {

        HashMap models = (HashMap) ((ArrayList) modelConfig.get("models")).get(0);

        return (CodegenModel) models.get("model");
    }

    private List<Schema> getObjectSchemas(final Schema schema, final Map<String, Schema> allDefinitions) {
        if (schema instanceof ObjectSchema) {
            return Lists.newArrayList(schema);
        } else if (schema instanceof ComposedSchema) {
            List<Schema> allOf = ((ComposedSchema) schema).getAllOf();
            if (allOf != null) {
                return allOf.stream().map(it -> getObjectSchemas(it, allDefinitions))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }
        } else if (schema.get$ref() != null) {
            return Lists.newArrayList(allDefinitions.get(ModelUtils.getSimpleRef(schema.get$ref())));
        }
        return Lists.newArrayList();
    }

    private List<Schema> getOneOf(final Schema schema, final Map<String, Schema> allDefinitions) {

        List<Schema> schemas = new ArrayList<>();

        if (schema instanceof ComposedSchema) {

            ComposedSchema composedSchema = (ComposedSchema) schema;
            for (Schema oneOfSchema : composedSchema.getOneOf()) {

                if (oneOfSchema.get$ref() != null) {

                    Schema refSchema = allDefinitions.get(ModelUtils.getSimpleRef(oneOfSchema.get$ref()));
                    if (refSchema instanceof ComposedSchema && ((ComposedSchema) refSchema).getOneOf() != null) {
                        schemas.addAll(((ComposedSchema) refSchema).getOneOf());
                    } else {
                        schemas.add(oneOfSchema);
                    }
                }
            }
        }

        return schemas;
    }

    private String[] getDiscriminatorKeys(final Schema schema, final Schema refSchema) {
        List<String> keys = new ArrayList<>();

        if (refSchema.getProperties() == null) {
            keys.add(schema.getDiscriminator().getPropertyName());
        } else {
            refSchema.getProperties().forEach((BiConsumer<String, Schema>) (property, propertySchema) -> {

                if (keys.isEmpty()) {
                    keys.add(property);

                } else if (propertySchema.getEnum() != null && propertySchema.getEnum().size() == 1) {
                    keys.add(property);
                }
            });
        }

        return keys.toArray(new String[0]);
    }

    public class TypeAdapter {

        public String classname;
        public String discriminator;
        public boolean isArray;
        public List<TypeAdapterItem> items = new ArrayList<>();
    }

    public class TypeAdapterItem {

        public String discriminatorValue;
        public String classname;
    }
}