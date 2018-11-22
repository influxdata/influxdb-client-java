package org.influxdata.platform;

/**
 * Write time-series data into InfluxData Platform 2.0.
 * <p>
 * The data are formatted in <a href="https://bit.ly/2QL99fu">Line Protocol</a>.
 *
 * @author Jakub Bednar (bednar@github) (22/11/2018 06:49)
 */
public interface WriteClientReactive extends AutoCloseable {

    /**
     * Close threads for asynchronous batch writing.
     */
    void close();
}