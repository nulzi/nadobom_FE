package org.pytorch.demo.objectdetection;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface APIConfig {
    @GET("update/check/{version}")
    Call<ResponseBody> checkUpdate(@Path("version") int version);

    @Multipart
    @POST("main/")
    Call<ResponseBody> reqObstacle(
            @Part("label") RequestBody text,
            @Part MultipartBody.Part image
    );
    @Multipart
    @POST("report/")
    Call<ResponseBody> reqReport(
            @Part("label") RequestBody text,
            @Part MultipartBody.Part image
    );
    static final String BASE_URL = "http://43.201.8.49:8000/"; // 주소 입력 부분
}
