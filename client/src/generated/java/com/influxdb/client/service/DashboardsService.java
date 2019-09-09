package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Cell;
import com.influxdb.client.domain.CellUpdate;
import com.influxdb.client.domain.CreateCell;
import com.influxdb.client.domain.CreateDashboardRequest;
import com.influxdb.client.domain.Dashboard;
import com.influxdb.client.domain.Dashboards;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.OperationLogs;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DashboardsService {
  /**
   * Delete a dashboard
   * 
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}")
  Call<Void> deleteDashboardsID(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a dashboard cell
   * 
   * @param dashboardID The ID of the dashboard to delete. (required)
   * @param cellID The ID of the cell to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/cells/{cellID}")
  Call<Void> deleteDashboardsIDCellsID(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a label from a dashboard
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param labelID The ID of the label to delete. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/labels/{labelID}")
  Call<Void> deleteDashboardsIDLabelsID(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove a member from a dashboard
   * 
   * @param userID The ID of the member to remove. (required)
   * @param dashboardID The dashboard ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/members/{userID}")
  Call<Void> deleteDashboardsIDMembersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove an owner from a dashboard
   * 
   * @param userID The ID of the owner to remove. (required)
   * @param dashboardID The dashboard ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/dashboards/{dashboardID}/owners/{userID}")
  Call<Void> deleteDashboardsIDOwnersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all dashboards
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param owner The owner ID. (optional)
   * @param sortBy The column to sort by. (optional)
   * @param id List of dashboard IDs to return. If both &#x60;id and &#x60;owner&#x60; are specified, only &#x60;id&#x60; is used. (optional, default to new ArrayList&lt;&gt;())
   * @param orgID The organization ID. (optional)
   * @param org The organization name. (optional)
   * @return Call&lt;Dashboards&gt;
   */
  @GET("api/v2/dashboards")
  Call<Dashboards> getDashboards(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("owner") String owner, @retrofit2.http.Query("sortBy") String sortBy, @retrofit2.http.Query("id") List<String> id, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("org") String org
  );

  /**
   * Get a Dashboard
   * 
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}")
  Call<Dashboard> getDashboardsID(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve the view for a cell
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param cellID The cell ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;View&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/cells/{cellID}/view")
  Call<View> getDashboardsIDCellsIDView(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * list all labels for a dashboard
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/labels")
  Call<LabelsResponse> getDashboardsIDLabels(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Retrieve operation logs for a dashboard
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param offset  (optional)
   * @param limit  (optional, default to 20)
   * @return Call&lt;OperationLogs&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/logs")
  Call<OperationLogs> getDashboardsIDLogs(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("offset") Integer offset, @retrofit2.http.Query("limit") Integer limit
  );

  /**
   * List all dashboard members
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/members")
  Call<ResourceMembers> getDashboardsIDMembers(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all dashboard owners
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("api/v2/dashboards/{dashboardID}/owners")
  Call<ResourceOwners> getDashboardsIDOwners(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a dashboard
   * 
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param dashboard Patching of a dashboard (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dashboards/{dashboardID}")
  Call<Dashboard> patchDashboardsID(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body Dashboard dashboard, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update the non-positional information related to a cell
   * Updates the non positional information related to a cell. Updates to a single cell&#39;s positional data could cause grid conflicts.
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param cellID The ID of the cell to update. (required)
   * @param cellUpdate  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Cell&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dashboards/{dashboardID}/cells/{cellID}")
  Call<Cell> patchDashboardsIDCellsID(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Body CellUpdate cellUpdate, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update the view for a cell
   * 
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param cellID The ID of the cell to update. (required)
   * @param view  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;View&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/dashboards/{dashboardID}/cells/{cellID}/view")
  Call<View> patchDashboardsIDCellsIDView(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Path("cellID") String cellID, @retrofit2.http.Body View view, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a dashboard
   * 
   * @param createDashboardRequest Dashboard to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards")
  Call<Dashboard> postDashboards(
    @retrofit2.http.Body CreateDashboardRequest createDashboardRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a dashboard cell
   * 
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param createCell Cell that will be added (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Cell&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/cells")
  Call<Cell> postDashboardsIDCells(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body CreateCell createCell, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to a dashboard
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/labels")
  Call<LabelResponse> postDashboardsIDLabels(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a member to a dashboard
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param addResourceMemberRequestBody User to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/members")
  Call<ResourceMember> postDashboardsIDMembers(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add an owner to a dashboard
   * 
   * @param dashboardID The dashboard ID. (required)
   * @param addResourceMemberRequestBody User to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/dashboards/{dashboardID}/owners")
  Call<ResourceOwner> postDashboardsIDOwners(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Replace cells in a dashboard
   * Replaces all cells in a dashboard. This is used primarily to update the positional information of all cells.
   * @param dashboardID The ID of the dashboard to update. (required)
   * @param cell  (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Dashboard&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PUT("api/v2/dashboards/{dashboardID}/cells")
  Call<Dashboard> putDashboardsIDCells(
    @retrofit2.http.Path("dashboardID") String dashboardID, @retrofit2.http.Body List<Cell> cell, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
