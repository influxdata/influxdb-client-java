package org.influxdata.client.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * TelegrafPlugin is the general wrapper of the telegraf plugin config.
 *
 * @author Jakub Bednar (bednar@github) (28/02/2019 09:08)
 */
public final class TelegrafPlugin {

    private String name;
    private String comment;
    private TelegrafPluginType type;
    private Map<String, Object> config = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public TelegrafPluginType getType() {
        return type;
    }

    public void setType(final TelegrafPluginType type) {
        this.type = type;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(final Map<String, Object> config) {
        this.config = config;
    }
}