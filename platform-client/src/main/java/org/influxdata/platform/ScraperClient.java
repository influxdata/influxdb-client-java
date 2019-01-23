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
package org.influxdata.platform;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.platform.domain.ScraperTarget;
import org.influxdata.platform.domain.ScraperTargetResponse;
import org.influxdata.platform.domain.ScraperType;

/**
 * Represents an HTTP API handler for scraper targets.
 *
 * @author Jakub Bednar (bednar@github) (22/01/2019 08:08)
 */
public interface ScraperClient {

    /**
     * Creates a new ScraperTarget and sets {@link ScraperTarget#getId()} with the new identifier.
     *
     * @param scraperTarget the scraper to create
     * @return ScraperTarget created
     */
    @Nonnull
    ScraperTargetResponse createScraperTarget(@Nonnull final ScraperTarget scraperTarget);

    /**
     * Create new ScraperTarget with {@link ScraperTarget#getType()} set to {@link ScraperType#PROMETHEUS}.
     *
     * @param name     the name of the new ScraperTarget
     * @param url      the url of the new ScraperTarget
     * @param bucketID the id of the bucket that its use to writes
     * @param orgID    the id of the organization that owns new ScraperTarget
     * @return ScraperTarget created
     */
    @Nonnull
    ScraperTargetResponse createScraperTarget(@Nonnull final String name,
                                              @Nonnull final String url,
                                              @Nonnull final String bucketID,
                                              @Nonnull final String orgID);

    /**
     * Update a ScraperTarget.
     *
     * @param scraperTarget ScraperTarget update to apply
     * @return ScraperTarget updated
     */
    @Nonnull
    ScraperTargetResponse updateScraperTarget(@Nonnull final ScraperTarget scraperTarget);

    /**
     * Delete a ScraperTarget.
     *
     * @param scraperTarget ScraperTarget to delete
     */
    void deleteScraperTarget(@Nonnull final ScraperTarget scraperTarget);

    /**
     * Delete a ScraperTarget.
     *
     * @param scraperTargetID ID of ScraperTarget to delete
     */
    void deleteScraperTarget(@Nonnull final String scraperTargetID);

    /**
     * Retrieve a ScraperTarget.
     *
     * @param scraperTargetID ID of ScraperTarget to get
     * @return ScraperTarget details
     */
    @Nullable
    ScraperTargetResponse findScraperTargetByID(@Nonnull final String scraperTargetID);

    /**
     * List all ScraperTargets.
     *
     * @return List all ScraperTargets
     */
    @Nonnull
    List<ScraperTargetResponse> findScraperTargets();
}