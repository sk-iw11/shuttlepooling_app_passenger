package com.example.trynumbertwo.network.api;

import com.example.trynumbertwo.network.model.AssignedBus;
import com.example.trynumbertwo.network.model.BusDemand;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IBusPoolingApi {

    @POST("demand")
    Call<Void> postDemand(@Body BusDemand demand);

    @GET("bus")
    Call<AssignedBus> getBus(@Query("departure") String departure, @Query("destination") String destination);
}
