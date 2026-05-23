package com.example.khetseva3.network
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import com.example.khetseva3.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
interface ApiService {

    // ✅ Normal API (keep this)
    @POST("recommend")
    fun getRecommendation(
        @Body request: RecommendRequest
    ): Call<RecommendResponse>

    // 🔥 ADD THIS (RAW DEBUG API)
    @POST("recommend")
    fun getRecommendationRaw(
        @Body request: RecommendRequest
    ): Call<ResponseBody>

    @GET("my-machines/{phone}")
    fun getMyMachines(
        @Path("phone") phone: String
    ): Call<MyMachinesResponse>

    @POST("machines/add")
    fun addMachine(
        @Body request: AddMachineRequest
    ): Call<Map<String, String>>

    @PUT("machines/update/{machine_id}")
    fun updateMachine(
        @Path("machine_id") machineId: Int,
        @Body request: AddMachineRequest
    ): Call<Map<String, String>>

    @POST("register")
    fun register(
        @Body request: RegisterRequest
    ): Call<Map<String, String>>

    @Multipart
    @POST("upload-image")
    fun uploadImage(
        @Part file: MultipartBody.Part
    ): Call<ImageUploadResponse>
    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>
}

