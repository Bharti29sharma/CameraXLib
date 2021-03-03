package com.example.myapplication;

import com.example.myapplication.model.HealthData;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIInterface {



//    @POST("/api/users")
//    Call<User> getHealthData(@Body User user);

//    @GET("/api/users?")
//    Call<UserList> doGetUserList(@Query("page") String page);

    @FormUrlEncoded
    @POST("/api/users?")
    Call<HealthData> getHealthData(@Field("value_array") int[] value_array, @Field("width") int width ,
                                   @Field("height") int height,@Field("n_channels") int n_channels);
}
