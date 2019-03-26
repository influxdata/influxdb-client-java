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
package org.influxdata.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.JavaClientCodegen;

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

    public InfluxJavaGenerator() {
        super();

        importMapping.put("JSON", "org.influxdata.client.JSON");
        importMapping.put("JsonDeserializer", "com.google.gson.JsonDeserializer");
        importMapping.put("JsonDeserializationContext", "com.google.gson.JsonDeserializationContext");
        importMapping.put("JsonParseException", "com.google.gson.JsonParseException");
        importMapping.put("ArrayList", "java.util.ArrayList");
        importMapping.put("List", "java.util.List");
        importMapping.put("JsonArray", "com.google.gson.JsonArray");
        importMapping.put("JsonElement", "com.google.gson.JsonElement");
        importMapping.put("HashMap", "java.util.HashMap");
        importMapping.put("Map", "java.util.Map");
        importMapping.put("ReflectType", "java.lang.reflect.Type");
    }

    @Override
    public Map<String, Object> postProcessAllModels(final Map<String, Object> models) {

        Map<String, String> pluginTypes = new HashMap<>();
        Map<String, String> pluginRequestTypes = new HashMap<>();

        models.forEach((modelName, modelConfig) -> {

            //
            // Set TelegrafPlugin and TelegrafRequestPlugin to generics
            //
            if (modelName.equals("TelegrafRequestPlugin") || modelName.equals("TelegrafPlugin")) {

                CodegenModel model = getModel((HashMap) modelConfig);
                model.setDiscriminator(null);

                //
                // Set name as generic
                //
                CodegenProperty nameProperty = model.getAllVars().get(0);
                nameProperty.dataType = "T";
                nameProperty.datatypeWithEnum = "T";

                model.allVars.get(model.allVars.size() - 1).hasMore = true;

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
                model.vars.add(configProperty);
                model.readWriteVars.add(configProperty);

                //
                // Add generics to class
                //
                model.vendorExtensions.put("x-has-generic-type", Boolean.TRUE);
                model.vendorExtensions.put("x-generic-type", "<T, C>");

                //
                // Use Plugin type from TelegrafRequestPlugin.TypeEnum
                //
                if (modelName.equals("TelegrafPlugin")) {
                    CodegenProperty typeProperty = getCodegenProperty(model, "type");
                    typeProperty.isEnum = false;
                    typeProperty.baseType = "TelegrafRequestPlugin.TypeEnum";
                    typeProperty.dataType = "TelegrafRequestPlugin.TypeEnum";
                    typeProperty.datatypeWithEnum = "TelegrafRequestPlugin.TypeEnum";
                }
            }

            //
            // Replace TelegrafPluginInputDiskioRequest, TelegrafPluginInputProcessesRequest, ... TelegrafPlugin*Request
            //
            if (modelName.startsWith("TelegrafPlugin") && modelName.endsWith("Request")) {

                HashMap requestConfig = (HashMap) modelConfig;
                CodegenModel requestModel = getModel(requestConfig);

                HashMap pluginConfig = (HashMap) models.get(requestModel.getInterfaces().get(1));
                CodegenModel pluginModel = getModel(pluginConfig);

                //
                // Copy properties for Plugin definition
                //
                requestModel.setDataType(pluginModel.dataType);
                List<CodegenProperty> properties = pluginModel.getVars().stream()
                        .filter(property -> !property.name.equals("type") && !property.name.equals("name") && !property.name.equals("config"))
                        .map(CodegenProperty::clone)
                        .collect(Collectors.toList());
                requestModel.setVars(properties);
                requestModel.hasVars = !requestModel.vars.isEmpty();

                requestModel.vars.get(requestModel.vars.size() - 1).hasMore = false;

                //
                // Set type of PluginConfiguration
                //
                CodegenProperty configProperty = getCodegenProperty(pluginModel, "config");
                String configType = configProperty != null ? configProperty.baseType : "Map<String, String>";

                //
                // Set generic parent type
                //
                requestModel.setParent(requestModel.parent + "<" + pluginModel.name + ".NameEnum, " + configType + ">");

                //
                // Set Name and Type in Constructor
                //
                ArrayList<Object> constructorItems = new ArrayList<>();
                constructorItems.add(String.format("setName(%s);", pluginModel.name + ".NameEnum." + getEnumDefaultValue(pluginModel, "name")));
                constructorItems.add(String.format("setType(%s);", "TelegrafRequestPlugin.TypeEnum." + getEnumDefaultValue(pluginModel, "type")));

                requestModel.vendorExtensions.put("x-has-constructor-items", Boolean.TRUE);
                requestModel.vendorExtensions.put("x-constructor-items", constructorItems);

                pluginRequestTypes.put(getEnumValue(pluginModel, "name"), requestModel.name + ".class");

                normalizeImports(requestConfig, pluginConfig);
            }

            //
            // Copy properties to Telegraf
            //
            if (modelName.equals("Telegraf")) {

                HashMap telegrafConfig = (HashMap) modelConfig;
                CodegenModel telegrafModel = getModel(telegrafConfig);

                HashMap requestConfig = (HashMap) models.get(telegrafModel.getInterfaces().get(0));
                CodegenModel requestModel = getModel(requestConfig);

                telegrafModel.setParent(null);
                telegrafModel.vars.get(telegrafModel.vars.size() - 1).hasMore = true;
                requestModel.getVars().stream().filter(property -> !property.name.equals("plugins"))
                        .forEach(requestProperty -> telegrafModel.vars.add(requestProperty.clone()));

            }
        });

        models.forEach((modelName, modelConfig) -> {

            //
            // Replace TelegrafPluginInputDiskio, TelegrafPluginInputProcesses, ... TelegrafPlugin*
            //
            if (modelName.startsWith("TelegrafPlugin") && !modelName.equals("TelegrafPlugin") && !modelName.endsWith("Request") && !modelName.toLowerCase().contains("config")) {

                CodegenModel pluginModel = getModel((HashMap) modelConfig);

                CodegenProperty configProperty = getCodegenProperty(pluginModel, "config");
                CodegenProperty typeProperty = getCodegenProperty(pluginModel, "type");
                CodegenProperty nameProperty = getCodegenProperty(pluginModel, "name");

                pluginModel.parent = "TelegrafPlugin<" + pluginModel.name + ".NameEnum, " + configProperty.baseType + ">";

                // Set Name and Type in Constructor
                ArrayList<Object> constructorItems = new ArrayList<>();
                constructorItems.add(String.format("setName(%s);", pluginModel.name + ".NameEnum." + getEnumDefaultValue(pluginModel, "name")));
                constructorItems.add(String.format("setType(%s);", "TelegrafRequestPlugin.TypeEnum." + getEnumDefaultValue(pluginModel, "type")));

                pluginModel.vendorExtensions.put("x-has-constructor-items", Boolean.TRUE);
                pluginModel.vendorExtensions.put("x-constructor-items", constructorItems);

                pluginModel.vendorExtensions.put("x-has-inner-enums", Boolean.TRUE);
                pluginModel.vendorExtensions.put("x-inner-enums", Arrays.asList(nameProperty));

                pluginTypes.put(getEnumValue(pluginModel, "name"), pluginModel.name + ".class");

                pluginModel.vars.remove(configProperty);
                pluginModel.vars.remove(typeProperty);
                pluginModel.vars.remove(nameProperty);
                pluginModel.vars.get(pluginModel.vars.size() - 1).hasMore = false;
            }
        });

        //
        // Configure Gson Type Selectors
        //
        HashMap<String, Object> pluginRequest = new HashMap<>();
        pluginRequest.put("name", "TelegrafRequestPlugin");
        pluginRequest.put("types", pluginRequestTypes.entrySet());

        HashMap<String, Object> plugin = new HashMap<>();
        plugin.put("name", "TelegrafPlugin");
        plugin.put("types", pluginTypes.entrySet());

        additionalProperties.put("vendorExtensions.x-type-selectors", Arrays.asList(pluginRequest, plugin));

        return super.postProcessAllModels(models);
    }

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles = supportingFiles.stream()
                .filter(supportingFile -> supportingFile.destinationFilename.equals("JSON.java"))
                .collect(Collectors.toList());
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
                    .filter(produce -> !produce.get("mediaType").equals("application/json"))
                    .map(produce -> {

                        switch (produce.get("mediaType")) {
                            default:
                            case "application/toml":
                            case "application/octet-stream":
                                return "ResponseBody";
                        }
                    })
                    .distinct()
                    .collect(Collectors.toList());

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

        String url;
        if (operation.getServers() != null)
        {
            url = operation.getServers().get(0).getUrl();
        }                                                       else {
            url = openAPI.getServers().get(0).getUrl();
        }

        if (!url.equals("/"))
        {
            op.path = url + op.path;
        }

        return op;
    }


    @Override
    public CodegenModel fromModel(final String name, final Schema model, final Map<String, Schema> allDefinitions) {
        CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);
        if (name.equals("Variable")) {
            model.getProperties().forEach(new BiConsumer<String, Schema>() {
                @Override
                public void accept(final String s, final Schema schema) {
                    if (schema instanceof ArraySchema) {
                        Schema items = ((ArraySchema) schema).getItems();

                        if (items instanceof ComposedSchema) {
                            CodegenProperty codegenProperty = getCodegenProperty(codegenModel, s);
                            String adapterName = name + codegenProperty.nameInCamelCase + "Adapter";

                            codegenProperty.vendorExtensions.put("x-has-type-adapter", Boolean.TRUE);
                            codegenProperty.vendorExtensions.put("x-type-adapter", adapterName);

                            Map<String, TypeAdapter> adapters = (HashMap<String, TypeAdapter>) codegenModel.vendorExtensions
                                    .getOrDefault("x-type-adapters", new HashMap<String, TypeAdapter>());

                            TypeAdapter typeAdapter = new TypeAdapter();
                            typeAdapter.classname = adapterName;
                            
                            adapters.put(adapterName, typeAdapter);

                            codegenModel.vendorExtensions.put("x-type-adapters", adapters);
                            codegenModel.imports.add("JsonDeserializer");
                            codegenModel.imports.add("ArrayList");
                            codegenModel.imports.add("List");
                            codegenModel.imports.add("JsonArray");
                            codegenModel.imports.add("JsonElement");
                            codegenModel.imports.add("JsonParseException");
                            codegenModel.imports.add("JsonDeserializationContext");
                            codegenModel.imports.add("Map");
                            codegenModel.imports.add("HashMap");
                            codegenModel.imports.add("ReflectType");
                        }
                    }
                }

            });
        }
        return codegenModel;
    }

    @Override
    public String toApiName(String name) {

        if (name.length() == 0) {
            return super.toApiName(name);
        }
        return camelize(name) + "Service";
    }

    private void normalizeImports(@Nonnull final HashMap requestConfig, @Nonnull final HashMap pluginConfig) {

        // add all
        ((List) requestConfig.get("imports")).addAll(((List) pluginConfig.get("imports")));

        // filter from same package
        List<HashMap<String, String>> filtered = ((List<HashMap<String, String>>) requestConfig.get("imports")).stream()
                .filter(imp -> !imp.get("import").startsWith("org.influxdata.client.domain"))
                .collect(Collectors.toList());

        // override
        requestConfig.put("imports", filtered);
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

        return enumValue.toUpperCase();
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

    public class TypeAdapter
    {
        public String classname;
    }
}