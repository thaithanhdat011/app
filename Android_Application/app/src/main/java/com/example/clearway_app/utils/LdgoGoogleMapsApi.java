package com.example.clearway_app.utils;

import com.example.clearway_app.helper.DistanceBetweenLocations;
import com.example.clearway_app.helper.SearchesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LdgoGoogleMapsApi {
    @GET("place/textsearch/json?key=AIzaSyBPOr1V_ffIyE9VXuVvmAzHJlEEx5mykU4")
    Call<SearchesResponse> searchForPlace(@Query("query") String query);

    @GET("distancematrix/json?key=AIzaSyBPOr1V_ffIyE9VXuVvmAzHJlEEx5mykU4")
    Call<DistanceBetweenLocations> getDistanceBetweenLocations(@Query("units") String units,
                                                               @Query("destinations") String destinations,
                                                               @Query("origins") String origins);

//    @GET("geocode/json?key=AIzaSyBPOr1V_ffIyE9VXuVvmAzHJlEEx5mykU4")
//    Call<GeoLocations> getDistanceBetweenLocations(@Query("latlng") String longitudesLatitudes);
}