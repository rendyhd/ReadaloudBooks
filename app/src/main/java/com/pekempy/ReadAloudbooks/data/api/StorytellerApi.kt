package com.pekempy.ReadAloudbooks.data.api

import retrofit2.http.*

interface StorytellerApi {
    @Multipart
    @POST("api/v2/token")
    suspend fun login(
        @Part("usernameOrEmail") username: okhttp3.RequestBody,
        @Part("password") password: okhttp3.RequestBody
    ): TokenResponse

    @GET("api/v2/books")
    suspend fun listBooks(
        @Query("synced") synced: Boolean = true
    ): List<BookResponse>

    @GET("api/v2/books/{uuid}")
    suspend fun getBookDetails(
        @Path("uuid") uuid: String
    ): BookResponse

    @GET("api/v2/books/{uuid}/positions")
    suspend fun getPosition(
        @Path("uuid") uuid: String
    ): Position?

    @POST("api/v2/books/{uuid}/positions")
    suspend fun updatePosition(
        @Path("uuid") uuid: String,
        @Body position: Position
    )

    @POST("api/v2/books/{uuid}/process")
    suspend fun processBook(
        @Path("uuid") uuid: String,
        @Query("restart") restart: Boolean? = null
    )
}
