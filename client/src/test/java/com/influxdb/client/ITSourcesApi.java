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

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.domain.Source;
import com.influxdb.exceptions.NotFoundException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (18/09/2018 09:42)
 */
@RunWith(JUnitPlatform.class)
class ITSourcesApi extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITSourcesApi.class.getName());

    private SourcesApi sourcesApi;

    @BeforeEach
    void setUp() {

        sourcesApi = influxDBClient.getSourcesApi();
        sourcesApi.findSources().stream()
                .filter(source -> !Boolean.TRUE.equals(source.getDefault()))
                .forEach(source -> sourcesApi.deleteSource(source));
    }

    @Test
    void createSource() {

        Source source = new Source();

        source.setOrgID("02cebf26d7fc1000");
        source.setDefault(false);
        source.setName(generateName("Source"));
        source.setType(Source.TypeEnum.V1);
        source.setUrl("http://localhost:8086");
        source.setInsecureSkipVerify(true);
        source.setTelegraf("telegraf");
        source.setToken(UUID.randomUUID().toString());
        source.setUsername("admin");
        source.setPassword("password");
        source.setSharedSecret(UUID.randomUUID().toString());
        source.setMetaUrl("/usr/local/var/influxdb/meta");
        source.setDefaultRP("autogen");

        Source createdSource = sourcesApi.createSource(source);

        LOG.log(Level.INFO, "Created source: {0}", createdSource);

        Assertions.assertThat(createdSource.getId()).isNotBlank();
        Assertions.assertThat(createdSource.getOrgID()).isEqualTo(source.getOrgID());
        Assertions.assertThat(createdSource.getDefault()).isEqualTo(source.getDefault());
        Assertions.assertThat(createdSource.getName()).isEqualTo(source.getName());
        Assertions.assertThat(createdSource.getType()).isEqualTo(source.getType());
        Assertions.assertThat(createdSource.getUrl()).isEqualTo(source.getUrl());
        Assertions.assertThat(createdSource.getInsecureSkipVerify()).isEqualTo(source.getInsecureSkipVerify()  );
        Assertions.assertThat(createdSource.getTelegraf()).isEqualTo(source.getTelegraf());
        Assertions.assertThat(createdSource.getToken()).isEqualTo(source.getToken());
        Assertions.assertThat(createdSource.getUsername()).isEqualTo(source.getUsername());
        Assertions.assertThat(createdSource.getPassword()).isNull();
        Assertions.assertThat(createdSource.getSharedSecret()).isNull();
        Assertions.assertThat(createdSource.getMetaUrl()).isEqualTo(source.getMetaUrl());
        Assertions.assertThat(createdSource.getDefaultRP()).isEqualTo(source.getDefaultRP());
    }

    @Test
    void updateSource() {

        Source source = newSource();
        source.setInsecureSkipVerify(false);

        source = sourcesApi.createSource(source);
        Assertions.assertThat(source.getInsecureSkipVerify()).isNull();

        source.setInsecureSkipVerify(true);
        source = sourcesApi.updateSource(source);

        Assertions.assertThat(source.getInsecureSkipVerify()).isTrue();
    }

    @Test
    void deleteSource() {

        Source createdSource = sourcesApi.createSource(newSource());
        Assertions.assertThat(createdSource).isNotNull();

        Source foundSource = sourcesApi.findSourceByID(createdSource.getId());
        Assertions.assertThat(foundSource).isNotNull();

        // delete source
        sourcesApi.deleteSource(createdSource);

        Assertions.assertThatThrownBy(() -> sourcesApi.findSourceByID(createdSource.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("source not found");

    }

    @Test
    void findSourceByID() {

        Source source = sourcesApi.createSource(newSource());

        Source sourceByID = sourcesApi.findSourceByID(source.getId());

        Assertions.assertThat(sourceByID).isNotNull();
        Assertions.assertThat(sourceByID.getId()).isEqualTo(source.getId());
        Assertions.assertThat(sourceByID.getName()).isEqualTo(source.getName());
        Assertions.assertThat(sourceByID.getOrgID()).isEqualTo(source.getOrgID());
        Assertions.assertThat(sourceByID.getType()).isEqualTo(source.getType());
        Assertions.assertThat(sourceByID.getUrl()).isEqualTo(source.getUrl());
        Assertions.assertThat(sourceByID.getInsecureSkipVerify()).isEqualTo(source.getInsecureSkipVerify());
    }

    @Test
    void findSourceByIDNull() {

        Assertions.assertThatThrownBy(() -> sourcesApi.findSourceByID("020f755c3d082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("source not found");
    }

    @Test
    void findSources() {

        int size = sourcesApi.findSources().size();

        sourcesApi.createSource(newSource());

        List<Source> sources = sourcesApi.findSources();
        Assertions.assertThat(sources).hasSize(size + 1);
    }

    @Test
    void findBucketsBySource() {

        Source source = sourcesApi.createSource(newSource());

        List<Bucket> buckets = sourcesApi.findBucketsBySource(source);

        Assertions.assertThat(buckets).isNotNull();
        Assertions.assertThat(buckets.size()).isGreaterThan(0);
    }

    @Test
    void findBucketsBySourceByUnknownSource() {

        Assertions.assertThatThrownBy(() -> sourcesApi.findBucketsBySourceID("020f755c3d082000"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("source not found");
    }

    @Test
    void sourceHealth() {

        Source source = sourcesApi.createSource(newSource());

		HealthCheck check = sourcesApi.health(source);

        Assertions.assertThat(check).isNotNull();
        Assertions.assertThat(check.getStatus()).isEqualTo(HealthCheck.StatusEnum.PASS);
    }

    @Test
    void cloneSource() {

        Source source = sourcesApi.createSource(newSource());

        String name = generateName("cloned");

        Source cloned = sourcesApi.cloneSource(name, source.getId());

        Assertions.assertThat(cloned.getName()).isEqualTo(name);
        Assertions.assertThat(cloned.getOrgID()).isEqualTo(source.getOrgID());
        Assertions.assertThat(cloned.getDefault()).isEqualTo(source.getDefault());
        Assertions.assertThat(cloned.getType()).isEqualTo(source.getType());
        Assertions.assertThat(cloned.getUrl()).isEqualTo(source.getUrl());
        Assertions.assertThat(cloned.getInsecureSkipVerify()).isEqualTo(source.getInsecureSkipVerify());
        Assertions.assertThat(cloned.getTelegraf()).isEqualTo(source.getTelegraf());
        Assertions.assertThat(cloned.getToken()).isEqualTo(source.getToken());
        Assertions.assertThat(cloned.getUsername()).isEqualTo(source.getUsername());
        Assertions.assertThat(cloned.getPassword()).isEqualTo(source.getPassword());
        Assertions.assertThat(cloned.getSharedSecret()).isEqualTo(source.getSharedSecret());
        Assertions.assertThat(cloned.getMetaUrl()).isEqualTo(source.getMetaUrl());
        Assertions.assertThat(cloned.getDefaultRP()).isEqualTo(source.getDefaultRP());
    }

    @Test
    void cloneSourceNotFound() {
        Assertions.assertThatThrownBy(() -> sourcesApi.cloneSource(generateName("cloned"), "da7aba5e5d81e550"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("source not found");
    }

    @Nonnull
    private Source newSource() {

        Source source = new Source();

        source.setName(generateName("Source"));
        source.setOrgID("02cebf26d7fc1000");
        source.setType(Source.TypeEnum.V1);
        source.setUrl("http://influxdb:8086");
        source.setInsecureSkipVerify(true);

        return source;
    }
}