package org.influxdata.client.domain;

import com.squareup.moshi.Json;

/**
 * Type is a telegraf plugin type.
 * 
 * @author Jakub Bednar (bednar@github) (28/02/2019 09:10)
 */
public enum TelegrafPluginType {

    @Json(name = "input")
    INPUT,

    @Json(name = "output")
    OUTPUT,

    @Json(name = "processor")
    PROCESSOR,

    @Json(name = "aggregator")
    AGGREGATOR
}