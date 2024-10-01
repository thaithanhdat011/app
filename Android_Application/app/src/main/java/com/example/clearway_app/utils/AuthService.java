package com.example.clearway_app.utils;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthService {

    @Headers("Content-Type: application/json")
    @POST("auth/signup")
    Call<Void> signUp(@Body SignUpRequest signUpRequest);
}

