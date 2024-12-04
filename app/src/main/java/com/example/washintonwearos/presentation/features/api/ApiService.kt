package com.example.washintonwearos.presentation.features.api

import com.example.washintonwearos.presentation.features.DataClasses.TransferOrder
import com.example.washintonwearos.presentation.features.DataClasses.TransferOrderDetails
import com.example.washintonwearos.presentation.features.ViewModels.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("warehouse_transfer/{id}")
    suspend fun getTransferOrderDetails(@Path("id") id: String): TransferOrderDetails

    //Only get the ones that have a status of "Approving"
    @GET("wearable/orders/")
    suspend fun getTransferOrder(): Response<List<TransferOrder>>

    @POST("wearable/reject/{orderID}")
    suspend fun rejectOrder(@Path("orderID") orderID: Int): Response<ApiResponse>

    @POST("wearable/approve/{orderID}")
    suspend fun approveOrder(@Path("orderID") orderID: Int): Response<ApiResponse>

    @POST("wearable/notify-app")
    suspend fun notifyApp(@Body data: NotificationData): Response<ApiResponse>

}
data class NotificationData(
    val type: String,
    val orderID: Int
)
