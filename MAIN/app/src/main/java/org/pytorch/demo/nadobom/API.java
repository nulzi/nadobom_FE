package org.pytorch.demo.nadobom;

import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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

interface UpdateCheckCallback {
    void onUpdateCheckCompleted(boolean version);
}
public class API {
    private static Call<ResponseBody> responseBodyCall;
    private static Retrofit retrofit;
    private static APIConfig apiConfig;

    public static void getUpdateCheck(int reqAppVersion, final UpdateCheckCallback callback){
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
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    boolean version = jsonObject.getBoolean("version");
                    callback.onUpdateCheckCompleted(version);
                    Log.d("MyTag","json: "+jsonObject.getBoolean("version"));
                } catch (Exception e){
                    e.printStackTrace();
                    callback.onUpdateCheckCompleted(true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("D_Test", "실패: " + t.toString());
                callback.onUpdateCheckCompleted(true);
            }
        });
    }
    public static void updateApp(){
        retrofit = new Retrofit.Builder()
                .baseUrl(APIConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiConfig = retrofit.create(APIConfig.class);
        responseBodyCall = apiConfig.updateApp();
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
                    Log.e("D_Test1", "실패: " + t.toString());
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
                    Log.e("D_Test2", "실패: " + t.toString());
                }
            });
        }
    }
}
