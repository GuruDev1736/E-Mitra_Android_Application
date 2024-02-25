package com.emitra.tutionnotesaplication.API

import com.emitra.tutionnotesaplication.Models.ApiModel.WikipediaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("w/api.php")
    fun getPageSummary(
        @Query("action") action: String = "query",
        @Query("format") format: String = "json",
        @Query("prop") prop: String = "extracts",
        @Query("exintro") exintro: Int = 1,
        @Query("explaintext") explaintext: Int = 1,
        @Query("titles") title: String
    ): Call<WikipediaResponse>
}