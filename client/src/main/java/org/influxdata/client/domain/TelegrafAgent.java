package org.influxdata.client.domain;

/**
 * TelegrafAgent is based telegraf/internal/config AgentConfig.
 *
 * @author Jakub Bednar (bednar@github) (28/02/2019 09:05)
 */
public final class TelegrafAgent {

    /**
     * Default data collection interval for all inputs in milliseconds.
     */
    private Integer collectionInterval;

    public Integer getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(final Integer collectionInterval) {
        this.collectionInterval = collectionInterval;
    }
}