package com.example.washintonwearos.presentation.features.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.washintonwearos.presentation.features.DataClasses.TransferOrder
import com.example.washintonwearos.presentation.features.DataClasses.TransferOrderDetails
import com.example.washintonwearos.presentation.features.api.NotificationData
import com.example.washintonwearos.presentation.features.api.RetrofitClient

class OrdersViewModel : ViewModel() {

    private val _orders = mutableStateOf<List<TransferOrder>>(emptyList())
    val orders: State<List<TransferOrder>> = _orders

    private val _orderDetails = mutableStateOf<TransferOrderDetails?>(null)
    val orderDetails: State<TransferOrderDetails?> = _orderDetails

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    init {
        fetchOrders()
    }

     fun fetchOrders() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getTransferOrder()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _orders.value = it
                    }
                } else {
                    // Handle errors like 404 or other HTTP errors
                }
            } catch (e: Exception) {
                e.printStackTrace() // Log or show error message
            }
        }
    }

     fun fetchOrderDetails(orderId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getTransferOrderDetails(orderId.toString())
                _orderDetails.value = response

            } catch (e: Exception) {
                e.printStackTrace() // Log or show error message
            }
        }
    }

    fun rejectOrder(orderID: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.rejectOrder(orderID)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _message.value = it.message
                    } ?: run {
                        _message.value = "Unknown error occurred"
                    }
                } else {
                    _message.value = "Failed to reject order: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun approveOrder(orderID: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.approveOrder(orderID)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _message.value = it.message
                    } ?: run {
                        _message.value = "Unknown error occurred"
                    }
                } else {
                    _message.value = "Failed to approve order: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun notifyApp(type: String,orderID: Int) {
        viewModelScope.launch {
            try {
                val data = NotificationData(type, orderID) // Create NotificationData object
                val response = RetrofitClient.apiService.notifyApp(data) // Call modified API function
                if (response.isSuccessful) {
                    response.body()?.let {
                        _message.value = "Notifying app..."
                    } ?: run {
                        _message.value = "Unknown error occurred"
                    }
                } else {
                    _message.value = "Failed to approve order: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

}

data class ApiResponse(
    val message: String
)