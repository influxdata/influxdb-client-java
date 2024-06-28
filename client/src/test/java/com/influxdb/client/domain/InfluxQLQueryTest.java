package com.influxdb.client.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class InfluxQLQueryTest {

  @Test
  public void headerSelectDefault(){
    InfluxQLQuery query = new InfluxQLQuery("SELECT * FROM cpu", "test_db");
    Assertions.assertThat(query.getAcceptHeaderVal()).isEqualTo("application/json");
  }

  @Test
  public void headerSelect(){
    InfluxQLQuery query = new InfluxQLQuery("SELECT * FROM cpu",
      "test_db",
      InfluxQLQuery.AcceptHeader.CSV);
    Assertions.assertThat(query.getAcceptHeaderVal()).isEqualTo("application/csv");
  }
}
