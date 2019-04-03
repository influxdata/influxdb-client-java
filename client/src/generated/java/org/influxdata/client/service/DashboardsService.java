package org.influxdata.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import org.influxdata.client.domain.AddResourceMemberRequestBody;
import org.influxdata.client.domain.Cell;
import org.influxdata.client.domain.CellUpdate;
import org.influxdata.client.domain.CreateCell;
import org.influxdata.client.domain.CreateDashboardRequest;
import org.influxdata.client.domain.Dashboard;
import org.influxdata.client.domain.Dashboards;
import org.influxdata.client.domain.Error;
import org.influxdata.client.domain.LabelMapping;
import org.influxdata.client.domain.LabelResponse;
import org.influxdata.client.domain.LabelsResponse;
import org.influxdata.client.domain.OperationLogs;
import org.influxdata.client.domain.ResourceMember;
import org.influxdata.client.domain.ResourceMembers;
import org.influxdata.client.domain.ResourceOwner;
import org.influxdata.client.domain.ResourceOwners;
import org.influxdata.client.domain.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DashboardsService {
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

  /**
   * Delete a dashboard
   * 
   * @param dashboardID ID of dashboard to update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}")
  Call<Void> dashboardsDashboardIDDelete(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get a single Dashboard
   * 
   * @param dashboardID ID of dashboard to update (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}")
  Call<Dashboard> dashboardsDashboardIDGet(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * list all labels for a dashboard
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/labels")
  Call<LabelsResponse> dashboardsDashboardIDLabelsGet(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * delete a label from a dashboard
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param labelID the label id to delete (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/labels/{labelID}")
  Call<Void> dashboardsDashboardIDLabelsLabelIDDelete(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * add a label to a dashboard
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param labelMapping label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/labels")
  Call<LabelResponse> dashboardsDashboardIDLabelsPost(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve operation logs for a dashboard
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;OperationLogs&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/logs")
  Call<OperationLogs> dashboardsDashboardIDLogsGet(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * List all dashboard members
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/members")
  Call<ResourceMembers> dashboardsDashboardIDMembersGet(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add dashboard member
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param addResourceMemberRequestBody user to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/members")
  Call<ResourceMember> dashboardsDashboardIDMembersPost(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes a member from an dashboard
   * 
   * @param userID ID of member to remove (required)
   * @param dashboardID ID of the dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/members/{userID}")
  Call<Void> dashboardsDashboardIDMembersUserIDDelete(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all dashboard owners
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/owners")
  Call<ResourceOwners> dashboardsDashboardIDOwnersGet(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add dashboard owner
   * 
   * @param dashboardID ID of the dashboard (required)
   * @param addResourceMemberRequestBody user to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/owners")
  Call<ResourceOwner> dashboardsDashboardIDOwnersPost(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes an owner from a dashboard
   * 
   * @param userID ID of owner to remove (required)
   * @param dashboardID ID of the dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/owners/{userID}")
  Call<Void> dashboardsDashboardIDOwnersUserIDDelete(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a single dashboard
   * 
   * @param dashboardID ID of dashboard to update (required)
   * @param dashboard patching of a dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dashboards/{dashboardID}")
  Call<Dashboard> dashboardsDashboardIDPatch(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body Dashboard dashboard, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all dashboards
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param owner specifies the owner id to return resources for (optional)
   * @param sortBy specifies the owner id to return resources for (optional)
   * @param id ID list of dashboards to return. If both this and owner are specified, only ids is used. (optional)
   * @param orgID specifies the organization id of the resource (optional)
   * @param org specifies the organization name of the resource (optional)
   * @return Call&lt;Dashboards&gt;
   */
  @GET("api/v2/dashboards")
  Call<Dashboards> dashboardsGet(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("owner") String owner, @retrofit2.http.Query("sortBy") String sortBy, @retrofit2.http.Query("id") List<String> id, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("org") String org
  );

  /**
   * Create a dashboard
   * 
   * @param createDashboardRequest dashboard to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards")
  Call<Dashboard> dashboardsPost(
    @retrofit2.http.Body CreateDashboardRequest createDashboardRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
