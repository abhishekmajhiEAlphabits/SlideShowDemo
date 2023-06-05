package com.example.slideshowdemo.network

import com.example.slideshowdemo.model.PlaylistData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiInterface {

    @GET("/api/signage/screen/{screen_code}/playlist")
    fun getPlayList(@Path(value = "screen_code", encoded = true) code: String): Call<List<PlaylistData>>

}