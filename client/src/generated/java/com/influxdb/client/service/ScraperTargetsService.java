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
   * delete a scraper target
   * 
   * @param scraperTargetID id of the scraper target (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scrapers/{scraperTargetID}")
  Call<Void> deleteScrapersID(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * delete a label from a scraper target
   * 
   * @param scraperTargetID ID of the scraper target (required)
   * @param labelID ID of the label (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scrapers/{scraperTargetID}/labels/{labelID}")
  Call<Void> deleteScrapersIDLabelsID(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Path("labelID") String labelID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes a member from a scraper target
   * 
   * @param userID ID of member to remove (required)
   * @param scraperTargetID ID of the scraper target (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scrapers/{scraperTargetID}/members/{userID}")
  Call<Void> deleteScrapersIDMembersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * removes an owner from a scraper target
   * 
   * @param userID ID of owner to remove (required)
   * @param scraperTargetID ID of the scraper target (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;Void&gt;
   */
  @DELETE("api/v2/scrapers/{scraperTargetID}/owners/{userID}")
  Call<Void> deleteScrapersIDOwnersID(
    @retrofit2.http.Path("userID") String userID, @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * get all scraper targets
   * 
   * @param zapTraceSpan OpenTracing span context (optional)
   * @param name specifies the name of the scraper target. (optional)
   * @param id ID list of scraper targets to return. If both this and owner are specified, only ids is used. (optional, default to new ArrayList&lt;&gt;())
   * @param orgID specifies the organization id of the scraper target (optional)
   * @param org specifies the organization name of the scraper target (optional)
   * @return Call&lt;ScraperTargetResponses&gt;
   */
  @GET("api/v2/scrapers")
  Call<ScraperTargetResponses> getScrapers(
    @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan, @retrofit2.http.Query("name") String name, @retrofit2.http.Query("id") List<String> id, @retrofit2.http.Query("orgID") String orgID, @retrofit2.http.Query("org") String org
  );

  /**
   * get a scraper target by id
   * 
   * @param scraperTargetID id of the scraper target (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ScraperTargetResponse&gt;
   */
  @GET("api/v2/scrapers/{scraperTargetID}")
  Call<ScraperTargetResponse> getScrapersID(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * list all labels for a scraper targets
   * 
   * @param scraperTargetID ID of the scraper target (required)
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
   * @param scraperTargetID ID of the scraper target (required)
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
   * @param scraperTargetID ID of the scraper target (required)
   * @param zapTraceSpan OpenTracing span context (optional)
   * @return Call&lt;ResourceOwners&gt;
   */
  @GET("api/v2/scrapers/{scraperTargetID}/owners")
  Call<ResourceOwners> getScrapersIDOwners(
    @retrofit2.http.Path("scraperTargetID") String scraperTargetID, @retrofit2.http.Header("Zap-Trace-Span") String zapTraceSpan
  );

  /**
   * update a scraper target
   * 
   * @param scraperTargetID id of the scraper target (required)
   * @param scraperTargetRequest scraper target update to apply (required)
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
   * update a label from a scraper target
   * 
   * @param scraperTargetID ID of the scraper target (required)
   * @param labelID ID of the label (required)
   * @param label label update to apply (required)
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
   * create a scraper target
   * 
   * @param scraperTargetRequest scraper target to create (required)
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
   * add a label to a scraper target
   * 
   * @param scraperTargetID ID of the scraper target (required)
   * @param labelMapping label to add (required)
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
   * Add scraper target member
   * 
   * @param scraperTargetID ID of the scraper target (required)
   * @param addResourceMemberRequestBody user to add as member (required)
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
   * Add scraper target owner
   * 
   * @param scraperTargetID ID of the scraper target (required)
   * @param addResourceMemberRequestBody user to add as owner (required)
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
