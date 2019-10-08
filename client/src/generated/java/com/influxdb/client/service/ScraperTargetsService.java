package com.influxdb.client.service;

import retrofit2.Call;
import retrofit2.http.*;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;

import com.influxdb.client.domain.AddResourceMemberRequestBody;
import com.influxdb.client.domain.Error;
import com.influxdb.client.domain.Label;
import com.influxdb.client.domain.LabelMapping;
import com.influxdb.client.domain.LabelResponse;
import com.influxdb.client.domain.LabelsResponse;
import com.influxdb.client.domain.ResourceMember;
import com.influxdb.client.domain.ResourceMembers;
import com.influxdb.client.domain.ResourceOwner;
import com.influxdb.client.domain.ResourceOwners;
import com.influxdb.client.domain.ScraperTargetRequest;
import com.influxdb.client.domain.ScraperTargetResponse;
import com.influxdb.client.domain.ScraperTargetResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ScraperTargetsService {
  /**
   * Delete a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scrapers/{scraperTargetID}")
  Call<Void> deleteScrapersID(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Delete a label from a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param labelID The label ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scrapers/{scraperTargetID}/labels/{labelID}")
  Call<Void> deleteScrapersIDLabelsID(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove a member from a scraper target
   * 
   * @param userID The ID of member to remove. (required)
   * @param scraperTargetID The scraper target ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scrapers/{scraperTargetID}/members/{userID}")
  Call<Void> deleteScrapersIDMembersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Remove an owner from a scraper target
   * 
   * @param userID The ID of owner to remove. (required)
   * @param scraperTargetID The scraper target ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scrapers/{scraperTargetID}/owners/{userID}")
  Call<Void> deleteScrapersIDOwnersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Get all scraper targets
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param name Specifies the name of the scraper target. (optional)
   * @param id List of scraper target IDs to return. If both &#x60;id&#x60; and &#x60;owner&#x60; are specified, only &#x60;id&#x60; is used. (optional, default to new ArrayList&lt;&gt;())
   * @param orgID Specifies the organization ID of the scraper target. (optional)
   * @param org Specifies the organization name of the scraper target. (optional)
   * @return Call&lt;ScraperTargetResponses&gt;
   */
  @GET("api/v2/scrapers")
  Call<ScraperTargetResponses> getScrapers(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("name") String name, @retrofit2.http.Query("id") List<String> id, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("org") String org
  );

  /**
   * Get a scraper target by ID
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ScraperTargetResponse&gt;
   */
  @GET("api/v2/scrapers/{scraperTargetID}")
  Call<ScraperTargetResponse> getScrapersID(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all labels for a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelsResponse&gt;
   */
  @GET("api/v2/scrapers/{scraperTargetID}/labels")
  Call<LabelsResponse> getScrapersIDLabels(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all users with member privileges for a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMembers&gt;
   */
  @GET("api/v2/scrapers/{scraperTargetID}/members")
  Call<ResourceMembers> getScrapersIDMembers(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * List all owners of a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("api/v2/scrapers/{scraperTargetID}/owners")
  Call<ResourceOwners> getScrapersIDOwners(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param scraperTargetRequest Scraper target update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ScraperTargetResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/scrapers/{scraperTargetID}")
  Call<ScraperTargetResponse> patchScrapersID(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Body ScraperTargetRequest scraperTargetRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Update a label on a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param labelID The label ID. (required)
   * @param label Label update to apply (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @PATCH("api/v2/scrapers/{scraperTargetID}/labels/{labelID}")
  Call<Void> patchScrapersIDLabelsID(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Body Label label, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Create a scraper target
   * 
   * @param scraperTargetRequest Scraper target to create (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ScraperTargetResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/scrapers")
  Call<ScraperTargetResponse> postScrapers(
    @retrofit2.http.Body ScraperTargetRequest scraperTargetRequest, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a label to a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param labelMapping Label to add (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;LabelResponse&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/scrapers/{scraperTargetID}/labels")
  Call<LabelResponse> postScrapersIDLabels(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Body LabelMapping labelMapping, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add a member to a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param addResourceMemberRequestBody User to add as member (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceMember&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/scrapers/{scraperTargetID}/members")
  Call<ResourceMember> postScrapersIDMembers(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * Add an owner to a scraper target
   * 
   * @param scraperTargetID The scraper target ID. (required)
   * @param addResourceMemberRequestBody User to add as owner (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwner&gt;
   */
  @Headers({
    "Content-Type:application/json"
  })
  @POST("api/v2/scrapers/{scraperTargetID}/owners")
  Call<ResourceOwner> postScrapersIDOwners(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Body AddResourceMemberRequestBody addResourceMemberRequestBody, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

}
