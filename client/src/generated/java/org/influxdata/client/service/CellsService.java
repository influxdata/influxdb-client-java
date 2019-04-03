package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.Cell;
import org.influxdata.client.domain.CellUpdate;
import org.influxdata.client.domain.CreateCell;
import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CellsService {
  /**
   * Delete a dashboard cell
   * 
   * @param dashboardID ID of dashboard to delte (required)
   * @param cellID ID of cell to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/cells/{cellID}")
  Call<Void> dashboardsDashboardIDCellsCellIDDelete(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update the non positional information related to a cell (because updates to a single cells positional data could cause grid conflicts)
   * 
   * @param dashboardID ID of dashboard to update (required)
   * @param cellID ID of cell to update (required)
   * @param cellUpdate updates the non positional information related to a cell (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Cell&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dashboards/{dashboardID}/cells/{cellID}")
  Call<Cell> dashboardsDashboardIDCellsCellIDPatch(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Body CellUpdate cellUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve the view for a cell in a dashboard
   * 
   * @param dashboardID ID of dashboard (required)
   * @param cellID ID of cell (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;View&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/cells/{cellID}/view")
  Call<View> dashboardsDashboardIDCellsCellIDViewGet(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update the view for a cell
   * 
   * @param dashboardID ID of dashboard to update (required)
   * @param cellID ID of cell to update (required)
   * @param view updates the view for a cell (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;View&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dashboards/{dashboardID}/cells/{cellID}/view")
  Call<View> dashboardsDashboardIDCellsCellIDViewPatch(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Body View view, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a dashboard cell
   * 
   * @param dashboardID ID of dashboard to update (required)
   * @param createCell cell that will be added (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Cell&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/cells")
  Call<Cell> dashboardsDashboardIDCellsPost(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body CreateCell createCell, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Replace a dashboards cells
   * 
   * @param dashboardID ID of dashboard to update (required)
   * @param cell batch replaces all of a dashboards cells (this is used primarily to update the positional information of all of the cells) (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/dashboards/{dashboardID}/cells")
  Call<Dashboard> dashboardsDashboardIDCellsPut(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body List<Cell> cell, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
