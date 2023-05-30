package org.pytorch.demo.objectdetection;

import android.util.Log;

import java.io.File;
import java.security.spec.ECField;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class API {
    private static Call<ResponseBody> responseBodyCall;
    private static Retrofit retrofit;
    private static APIConfig apiConfig;

    public static void getUpdateCheck(int reqAppVersion){
        retrofit = new Retrofit.Builder()
                .baseUrl(APIConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiConfig = retrofit.create(APIConfig.class);
        responseBodyCall = apiConfig.checkUpdate(reqAppVersion);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Log.d("MyTag","body: "+response.body().string());
                    Log.d("MyTag","code: "+response.code());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("D_Test", "실패: " + t.toString());
            }
        });
    }
    public static void postObstacleData(File image, ArrayList<String> resultList) {
        retrofit = new Retrofit.Builder()
                .baseUrl(APIConfig.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiConfig = retrofit.create(APIConfig.class);

        if (image.exists() && !resultList.isEmpty()) {
            String info = "";
            for(String result : resultList){
                info = info + result;
                info = info + "\n";
            }
//            Log.d("MyTag", "info "+info);

            RequestBody reqFile = RequestBody.create(image, MediaType.parse("multipart/form-data"));
            MultipartBody.Part reqImage = MultipartBody.Part.createFormData("image", image.getName(), reqFile);
            RequestBody reqInfo = RequestBody.create((info), MediaType.parse("text/plain"));

//            Log.d("MyTag","post start");
            responseBodyCall = apiConfig.reqObstacle(reqInfo, reqImage);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String str = response.body().string();
//                        Log.d("MyTag","response : "+ str);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("D_Test", "실패: " + t.toString());
                }
            });
        }
    }

    public static void postReportData(File image, String location) {
        retrofit = new Retrofit.Builder()
                .baseUrl(APIConfig.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiConfig = retrofit.create(APIConfig.class);

        if (image.exists()) {
            RequestBody reqFile = RequestBody.create(image, MediaType.parse("multipart/form-data"));
            MultipartBody.Part reqImage = MultipartBody.Part.createFormData("image", image.getName(), reqFile);
            RequestBody reqLocation = RequestBody.create((location), MediaType.parse("text/plain"));

//            Log.d("MyTag","post start");
            responseBodyCall = apiConfig.reqReport(reqLocation, reqImage);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String str = response.body().string();
//                        Log.d("MyTag","response : "+ str);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("D_Test", "실패: " + t.toString());
                }
            });
        }
    }
}
