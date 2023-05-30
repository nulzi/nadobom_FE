package org.pytorch.demo.nadobom;

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

    @GET("update/excute")
    Call<ResponseBody> updateApp();
    @Multipart
    @POST("main/")
    Call<ResponseBody> reqObstacle(
            @Part("label") RequestBody text,
            @Part MultipartBody.Part image
    );
    @Multipart
    @POST("report/")
    Call<ResponseBody> reqReport(
            @Part("location") RequestBody text,
            @Part MultipartBody.Part image
    );
    static final String BASE_URL = "http://3.35.9.244:8000/"; // 주소 입력 부분
}
