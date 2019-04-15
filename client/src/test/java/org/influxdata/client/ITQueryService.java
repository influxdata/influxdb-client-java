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
package org.influxdata.client;

import java.io.IOException;
import java.util.Map;

import org.influxdata.client.domain.ASTResponse;
import org.influxdata.client.domain.AnalyzeQueryResponse;
import org.influxdata.client.domain.CallExpression;
import org.influxdata.client.domain.DurationLiteral;
import org.influxdata.client.domain.ExpressionStatement;
import org.influxdata.client.domain.FluxSuggestion;
import org.influxdata.client.domain.FluxSuggestions;
import org.influxdata.client.domain.Identifier;
import org.influxdata.client.domain.LanguageRequest;
import org.influxdata.client.domain.ObjectExpression;
import org.influxdata.client.domain.PipeExpression;
import org.influxdata.client.domain.Property;
import org.influxdata.client.domain.Query;
import org.influxdata.client.domain.QuerySpecification;
import org.influxdata.client.domain.UnaryExpression;
import org.influxdata.client.service.QueryService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (08/04/2019 09:41)
 */
@RunWith(JUnitPlatform.class)
class ITQueryService extends AbstractITClientTest {

    private QueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = influxDBClient.getService(QueryService.class);
    }

    @Test
    void spec() throws IOException {

        LanguageRequest languageRequest = new LanguageRequest().query("from(bucket: \"telegraf\")\n"
                + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))"
                + " |> range(start: -1d)");

        QuerySpecification specification = queryService.querySpecPost(null, null, languageRequest)
                .execute().body();

        Assertions.assertThat(specification).isNotNull();
        Assertions.assertThat(specification.getSpec()).isNotNull();

        // Operations
        Assertions.assertThat(specification.getSpec().getOperations()).hasSize(3);
        Assertions.assertThat(specification.getSpec().getOperations().get(0).getKind()).isEqualTo("influxDBFrom");
        Assertions.assertThat(specification.getSpec().getOperations().get(0).getId()).isEqualTo("influxDBFrom0");
        Assertions.assertThat(((Map<String, Object>) specification.getSpec().getOperations().get(0).getSpec()).get("bucket")).isEqualTo("telegraf");
        Assertions.assertThat(specification.getSpec().getOperations().get(1).getKind()).isEqualTo("filter");
        Assertions.assertThat(specification.getSpec().getOperations().get(1).getId()).isEqualTo("filter1");
        Assertions.assertThat(((Map<String, Object>) specification.getSpec().getOperations().get(1).getSpec()).get("fn")).isInstanceOf(Map.class);
        Assertions.assertThat(specification.getSpec().getOperations().get(2).getKind()).isEqualTo("range");
        Assertions.assertThat(specification.getSpec().getOperations().get(2).getId()).isEqualTo("range2");
        Assertions.assertThat(((Map<String, Object>) specification.getSpec().getOperations().get(2).getSpec()).get("start")).isEqualTo("-24h0m0s");
        Assertions.assertThat(((Map<String, Object>) specification.getSpec().getOperations().get(2).getSpec()).get("stop")).isEqualTo("now");
        Assertions.assertThat(((Map<String, Object>) specification.getSpec().getOperations().get(2).getSpec()).get("timeColumn")).isEqualTo("_time");
        Assertions.assertThat(((Map<String, Object>) specification.getSpec().getOperations().get(2).getSpec()).get("startColumn")).isEqualTo("_start");
        Assertions.assertThat(((Map<String, Object>) specification.getSpec().getOperations().get(2).getSpec()).get("stopColumn")).isEqualTo("_stop");

        // Edges
        Assertions.assertThat(specification.getSpec().getEdges()).hasSize(2);
        Assertions.assertThat(specification.getSpec().getEdges().get(0).getParent()).isEqualTo("influxDBFrom0");
        Assertions.assertThat(specification.getSpec().getEdges().get(0).getChild()).isEqualTo("filter1");
        Assertions.assertThat(specification.getSpec().getEdges().get(1).getParent()).isEqualTo("filter1");
        Assertions.assertThat(specification.getSpec().getEdges().get(1).getChild()).isEqualTo("range2");

        // Resources
        Assertions.assertThat(specification.getSpec().getResources()).isNotNull();
        Assertions.assertThat(specification.getSpec().getResources().getPriority()).isEqualTo("high");
        Assertions.assertThat(specification.getSpec().getResources().getConcurrencyQuota()).isEqualTo(0);
        Assertions.assertThat(specification.getSpec().getResources().getMemoryBytesQuota()).isEqualTo(0);

        //TODO https://github.com/influxdata/influxdb/issues/13217
        // Assertions.assertThat(specification.getSpec().getDialect()).isNotNull();
    }

    @Test
    void analyze() throws IOException {

        Query query = new Query().query("from(bucket: \"telegraf\")"
                + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))"
                + " |> range(start: -1d)");

        AnalyzeQueryResponse analyze = queryService.queryAnalyzePost(null, null, query).execute().body();

        Assertions.assertThat(analyze).isNotNull();
        Assertions.assertThat(analyze.getErrors()).isEmpty();

        query.query("from(bucket: \"telegraf\")"
                + " |> filter(fn: (r) => (r[\"_measurement\"] !=!=! \"cpu\"))"
                + " |> range(start: -1d)");

        analyze = queryService.queryAnalyzePost(null, null, query).execute().body();

        Assertions.assertThat(analyze).isNotNull();

        //TODO https://github.com/influxdata/influxdb/issues/13218
        // Assertions.assertThat(analyze.getErrors()).hasSize(1);
        // Assertions.assertThat(analyze.getErrors().get(0).getLine()).isEqualTo(2);
        // Assertions.assertThat(analyze.getErrors().get(0).getColumn()).isEqualTo(25);
        // Assertions.assertThat(analyze.getErrors().get(0).getCharacter()).isEqualTo(45);
        // Assertions.assertThat(analyze.getErrors().get(0).getMessage()).isEqualTo("");
    }

    @Test
    void ast() throws IOException {

        LanguageRequest languageRequest = new LanguageRequest().query("from(bucket: \"telegraf\")\n"
                + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))"
                + " |> range(start: -1d)");

        ASTResponse ast = queryService.queryAstPost(null, null, languageRequest)
                .execute().body();

        Assertions.assertThat(ast).isNotNull();
        Assertions.assertThat(ast.getAst()).isNotNull();
        Assertions.assertThat(ast.getAst().getType()).isEqualTo("Package");
        Assertions.assertThat(ast.getAst().getPackage()).isEqualTo("main");
        Assertions.assertThat(ast.getAst().getPath()).isNull();
        Assertions.assertThat(ast.getAst().getFiles()).hasSize(1);
        Assertions.assertThat(ast.getAst().getFiles().get(0).getType()).isEqualTo("File");
        Assertions.assertThat(ast.getAst().getFiles().get(0).getPackage()).isNull();
        Assertions.assertThat(ast.getAst().getFiles().get(0).getImports()).isNull();
        Assertions.assertThat(ast.getAst().getFiles().get(0).getBody()).hasSize(1);
        ExpressionStatement expressionStatement = (ExpressionStatement) ast.getAst().getFiles().get(0).getBody().get(0);
        Assertions.assertThat(expressionStatement.getType()).isEqualTo("ExpressionStatement");

        PipeExpression expression = (PipeExpression) expressionStatement.getExpression();
        Assertions.assertThat(expression.getType()).isEqualTo("PipeExpression");

        PipeExpression argument = (PipeExpression) expression.getArgument();
        Assertions.assertThat(argument.getType()).isEqualTo("PipeExpression");

        CallExpression call = expression.getCall();
        Identifier callee = (Identifier) call.getCallee();
        Assertions.assertThat(callee.getName()).isEqualTo("range");
        Assertions.assertThat(call.getArguments()).hasSize(1);

        ObjectExpression callArgument = (ObjectExpression) call.getArguments().get(0);
        Assertions.assertThat(callArgument.getProperties()).hasSize(1);

        Property property = callArgument.getProperties().get(0);
        Identifier key = (Identifier) property.getKey();
        Assertions.assertThat(key.getName()).isEqualTo("start");

        UnaryExpression value = (UnaryExpression) property.getValue();
        Assertions.assertThat(value.getOperator()).isEqualTo("-");

        DurationLiteral unaryArgument = (DurationLiteral) value.getArgument();
        Assertions.assertThat(unaryArgument.getValues()).hasSize(1);
        Assertions.assertThat(unaryArgument.getValues().get(0).getMagnitude()).isEqualTo(1);
        Assertions.assertThat(unaryArgument.getValues().get(0).getUnit()).isEqualTo("d");
    }

    @Test
    void suggestions() throws IOException {

        FluxSuggestions suggestions = queryService.querySuggestionsGet(null).execute().body();

        Assertions.assertThat(suggestions).isNotNull();
        Assertions.assertThat(suggestions.getFuncs().size()).isGreaterThan(140);

        FluxSuggestion pivot = suggestions.getFuncs().stream().filter(fluxSuggestionsFuncs -> fluxSuggestionsFuncs.getName().equals("pivot")).findFirst().get();
        Assertions.assertThat(pivot).isNotNull();
        Assertions.assertThat(pivot.getName()).isEqualTo("pivot");
        Assertions.assertThat(pivot.getParams())
                .hasSize(4)
                .hasEntrySatisfying("columnKey", value -> Assertions.assertThat(value).isEqualTo("array"))
                .hasEntrySatisfying("rowKey", value -> Assertions.assertThat(value).isEqualTo("array"))
                .hasEntrySatisfying("tables", value -> Assertions.assertThat(value).isEqualTo("object"))
                .hasEntrySatisfying("valueColumn", value -> Assertions.assertThat(value).isEqualTo("string"));

        FluxSuggestion suggestion = queryService.querySuggestionsNameGet("range", null).execute().body();
        Assertions.assertThat(suggestion).isNotNull();
        Assertions.assertThat(suggestion.getParams())
                .hasSize(6)
                .hasEntrySatisfying("start", value -> Assertions.assertThat(value).isEqualTo("invalid"))
                .hasEntrySatisfying("startColumn", value -> Assertions.assertThat(value).isEqualTo("string"))
                .hasEntrySatisfying("stop", value -> Assertions.assertThat(value).isEqualTo("invalid"))
                .hasEntrySatisfying("stopColumn", value -> Assertions.assertThat(value).isEqualTo("string"))
                .hasEntrySatisfying("tables", value -> Assertions.assertThat(value).isEqualTo("object"))
                .hasEntrySatisfying("timeColumn", value -> Assertions.assertThat(value).isEqualTo("string"));

    }
}