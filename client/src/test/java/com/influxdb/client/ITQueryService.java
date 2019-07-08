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
package com.influxdb.client;

import java.io.IOException;

import com.influxdb.client.domain.ASTResponse;
import com.influxdb.client.domain.AnalyzeQueryResponse;
import com.influxdb.client.domain.CallExpression;
import com.influxdb.client.domain.DurationLiteral;
import com.influxdb.client.domain.ExpressionStatement;
import com.influxdb.client.domain.FluxSuggestion;
import com.influxdb.client.domain.FluxSuggestions;
import com.influxdb.client.domain.Identifier;
import com.influxdb.client.domain.LanguageRequest;
import com.influxdb.client.domain.ObjectExpression;
import com.influxdb.client.domain.PipeExpression;
import com.influxdb.client.domain.Property;
import com.influxdb.client.domain.Query;
import com.influxdb.client.domain.UnaryExpression;
import com.influxdb.client.service.QueryService;

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
    void analyze() throws IOException {

        Query query = new Query().query("from(bucket: \"telegraf\")"
                + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))"
                + " |> range(start: -1d)");

        AnalyzeQueryResponse analyze = queryService.postQueryAnalyze(null, null, query).execute().body();

        Assertions.assertThat(analyze).isNotNull();
        Assertions.assertThat(analyze.getErrors()).isEmpty();

        query.query("from(bucket: \"telegraf\")"
                + " |> filter(fn: (r) => (r[\"_measurement\"] !=!=! \"cpu\"))"
                + " |> range(start: -1d)");

        analyze = queryService.postQueryAnalyze(null, null, query).execute().body();

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

        ASTResponse ast = queryService.postQueryAst(null, null, languageRequest)
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

        FluxSuggestions suggestions = queryService.getQuerySuggestions(null).execute().body();

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

        FluxSuggestion suggestion = queryService.getQuerySuggestionsName("range", null).execute().body();
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