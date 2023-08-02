package org.pytorch.demo.nadobom;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// 이와 같이 <타입>이 가능한 이유는 해당 클래스가 static 클래스이기 때문이다.
public class ObjectDetectionActivity extends AbstractCameraXActivity<ObjectDetectionActivity.AnalysisResult> implements TextToSpeech.OnInitListener {
    private Module mModule = null;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    // 객체 탐지 결과 화면
    private ResultView mResultView;
    private TextToSpeech textToSpeech;
    private TextView tvObstacle;
    private Button btnHelp;
    private Button btnReport;
    private Button btnEnd;
    private boolean option_help;
    private double longitude;
    private double latitude;
    private final int maxImageWidth = 640;
    private final int maxImageHeight = 640;

    final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            longitude = Math.round(location.getLongitude()*1000)/1000.0;
            latitude = Math.round(location.getLatitude()*1000)/1000.0;
//            Log.d("MyTag", "listener: 위도: " + latitude+ ", 경도: " + longitude);
        }
    };

    // 탐지 결과 저장 클래스
    static class AnalysisResult {
        private final ArrayList<Result> mResults;

        public AnalysisResult(ArrayList<Result> results) {
            mResults = results;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textToSpeech = new TextToSpeech(this, this);
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    ObjectDetectionActivity.this,
                    PERMISSIONS,
                    0);
        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
//                Log.d("MyTag", "listener: 위도: " + latitude+ ", 경도: " + longitude);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        }

        option_help = sharedPreferences.getBoolean("helpOption", SettingOption.helpOption);
        btnHelp = findViewById(R.id.btn_help_main);
        if (!option_help) btnHelp.setVisibility(View.INVISIBLE);
        else {
            btnHelp.setVisibility(View.VISIBLE);
        }
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak("다음은 장애물 탐지 화면 도움말입니다 장애물 안내텍스트, 신고를 위한 신고 버튼, 장애물 탐지 종료버튼이 있습니다", TextToSpeech.QUEUE_ADD, null, "helpComment");
            }
        });

        tvObstacle = findViewById(R.id.tv_obstacle);
        tvObstacle.setText("장애물 탐색 중");
        tvObstacle.setTextSize(Dimension.SP, sharedPreferences.getInt("textSize", SettingOption.textSize));
        tvObstacle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == null) Log.e("afterTextChanged", "null exception");
                if (!editable.toString().equals("result") && !editable.toString().equals("장애물 탐색 중")) {
                    textToSpeech.speak(editable.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        btnReport = findViewById(R.id.btn_report);
        btnReport.setTextSize(Dimension.SP, sharedPreferences.getInt("textSize", SettingOption.textSize));
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 인터넷 확인 조건 만들기
                reportTime = SystemClock.elapsedRealtime();
                isReport = true;
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak("신고를 시작합니다 카메라를 바닥으로 향하고 3초간 대기해주세요", TextToSpeech.QUEUE_FLUSH, null, "report");
//                Log.d("MyTag","위치 위도: "+ latitude + "경도: " + longitude);
            }
        });

        btnEnd = findViewById(R.id.btn_end);
        btnEnd.setTextSize(Dimension.SP, sharedPreferences.getInt("textSize",SettingOption.textSize));
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            float option_speechSpeed = sharedPreferences.getFloat("speechSpeed",SettingOption.speechSpeed);
            textToSpeech.setLanguage(Locale.KOREAN);
            textToSpeech.setPitch(1.0f);
            textToSpeech.setSpeechRate(option_speechSpeed);
//            textToSpeech.speak("장애물 탐색을 시작하겠습니다.", TextToSpeech.QUEUE_FLUSH, null, "mainComment");
//            if (option_help) textToSpeech.speak("다음은 장애물 탐지 화면 도움말입니다 장애물 안내텍스트, 신고를 위한 신고 버튼, 장애물 탐지 종료버튼이 있습니다", TextToSpeech.QUEUE_ADD, null, "helpComment");
        } else Log.e("MyTag", "TTS initialization fail");
    }
    @Override
    protected void onDestroy() {
        textToSpeech.stop();
        resetCacheDir("_od");
        resetCacheDir("_report");
        super.onDestroy();
    }

    // 최종 화면 선택
    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_object_detection;
    }

    // 카메라 영상 표시할 뷰 리턴
    @Override
    protected TextureView getCameraPreviewTextureView() {
        mResultView = findViewById(R.id.resultView);
        //object_detection_texture_view_stub에 object_detection_texture_view를 띄운다.
        return ((ViewStub) findViewById(R.id.object_detection_texture_view_stub))
                .inflate()
                .findViewById(R.id.object_detection_texture_view);
    }

    private void resetCacheDir(String dataType) {
        File file = new File(getCacheDir().toString());

        File[] files = file.listFiles();

        String path = getCacheDir() + "/";
        for (File tempFile : files) {
            if (tempFile.getName().contains(dataType)) {
                deleteImg(path + tempFile.getName());
            }
        }
    }

    @Nullable
    private File getFileFromCacheDir(String dataType){
        ArrayList<String> ods = new ArrayList<>();

        File file = new File(getCacheDir().toString());

        File[] files = file.listFiles();

        for(File tempFile : files){
            if(tempFile.getName().contains(dataType)){
                ods.add(tempFile.getName());
            }
        }

        if (ods.size() <= 0) return null;
        String path = getCacheDir() + "/";
        // 서버로 보낸 이미지 삭제
        if (ods.size() > 1) {
            long name1 = 0;
            long name2 = 0;
            String[] array = ods.get(0).split("_");
            name1 = Long.parseLong(array[0]);
            array = ods.get(1).split("_");
            name2 = Long.parseLong(array[0]);
            if (name1 < name2) {
                deleteImg(path + ods.get(0));
                ods.remove(0);
            } else {
                deleteImg(path + ods.get(1));
                ods.remove(1);
            }
        }
        return new File(getCacheDir(), ods.get(0));
    }

    private ArrayList<String> normalization(ArrayList<Result> results, int viewWidth, int viewHeight) {
        ArrayList<String> list = new ArrayList<>();
        for (Result result : results) {
            double x = (result.rect.left + result.rect.right) / (double) (viewWidth * 2);
            double y = (result.rect.top + result.rect.bottom) / (double) (viewHeight * 2);
            double w = (result.rect.right - result.rect.left) / (double) viewWidth;
            double h = (result.rect.bottom - result.rect.top) / (double) viewHeight;
            list.add(result.classIndex + " " + x + " " + y + " " + w + " " + h);
        }
        return list;
    }

    @Override
    protected void sendReportData(Bitmap image) {
        Log.d("MyTag","report start");
        if(image == null) {
            isReport = false;
            return;
        }

        saveImageToJpeg(image, SystemClock.elapsedRealtime(), "_report");
        File file = getFileFromCacheDir("_report");
        String location = latitude + "," + longitude + "," + getAddress(latitude,longitude);
        Log.d("MyTag",location);

//        Log.d("MyTag",location);
        if(file != null) API.postReportData(file, location);
        // 안내음 처리는 잘 모르겠다
        textToSpeech.speak("신고가 완료됐습니다",TextToSpeech.QUEUE_ADD,null,"reportEnd");

        isReport = false;
    }

    private String getAddress(double latitude, double longitude) {
        String nowAddr = "실제 주소";
        Geocoder geocoder = new Geocoder(ObjectDetectionActivity.this, Locale.KOREAN);
        List<Address> address;

        try {
            if(geocoder!=null){
                Log.d("MyTag","위치 위도: "+ latitude + "경도: " + longitude);
                address = geocoder.getFromLocation(latitude, longitude,1);
                if(address != null && address.size() >0) {
                    nowAddr = address.get(0).getAddressLine(0).toString();
//                    nowAddr.replace(" ","");
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return nowAddr;
    }
    @Override
    protected long sendObstacleData(Bitmap image, long time, AnalysisResult result) {
        Log.d("MyTag","result size: "+result.mResults.size());
        if (result.mResults.size() <= 0 || image == null) return time;
        saveImageToJpeg(image, time, "_od");
        File file = getFileFromCacheDir("_od");
        ArrayList<String> resultList = normalization(result.mResults, viewWidth, viewHeight);
        if (file != null) API.postObstacleData(file, resultList);

        return SystemClock.elapsedRealtime();
    }

    private void deleteImg(String path){
        File file = new File(path);
        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (isDeleted) {
                // 파일 삭제 성공
//                Log.e("MyTag","delete complete " + path);
            } else {
                // 파일 삭제 실패
//                Log.e("MyTag","delete fail " + path);
            }
        } else {
            // 파일이 존재하지 않음
//            Log.e("MyTag","file not exist " + path);
        }
    }

    private String makeResultText(ArrayList<String> results) {
        if (results.size() < 2) return "장애물 탐색 중";

        if (results.size() == 2) return results.get(0) + " " + results.get(1);

        String location1 = results.get(0);
        String result1 = results.get(1);
        String location2 = results.get(2);
        String result2 = results.get(3);
//        String count = results.get(4);

//        Log.d("MyTag", location1 + " " + result1 + ", " + location2 + " " + result2);
        if (location1.equals(location2) && result1.equals(result2))
            return location1 + " " + result1 + " 2개";

        if (location1.equals(location2)) return location1 + " " + result1 + ", 그리고 " + result2;

        return location1 + " " + result1 + ", " + location2 + " " + result2;
    }

    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
        if(!textToSpeech.isSpeaking()) {
            mResultView.setResults(result.mResults);
            mResultView.invalidate();
            if (!result.mResults.isEmpty())
                tvObstacle.setText(makeResultText(Priority.priority(Priority.input(result.mResults, viewHeight), viewWidth, viewHeight)));
            else tvObstacle.setText("장애물 탐색 중");
        }
    }

    private Bitmap imgToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
    protected void saveImageToJpeg(Bitmap image, long time, String dataType) {
        // 이미지 크기 조절 전 작업
        Bitmap resizedImage = null;

        if(image.getWidth() > maxImageWidth || image.getHeight() > maxImageHeight) {
            float widthRatio = (float) maxImageWidth / image.getWidth();
            float heightRatio = (float) maxImageHeight / image.getHeight();
            float scale = Math.min(widthRatio, heightRatio);
            int newWidth = Math.round(image.getWidth() * scale);
            int newHeight = Math.round(image.getHeight() * scale);
//            Log.d("MyTag","scale"+scale);
//            Log.d("MyTag","new width, height"+newWidth+", "+newHeight);
            resizedImage = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
        }

        // Bitmap to Jpeg
        File storage = getCacheDir();
        String timeString = Long.toString(time);
        String fileName = timeString + dataType + ".jpg";

        File tempFile = new File(storage, fileName);

        try {
            tempFile.createNewFile();

            FileOutputStream out = new FileOutputStream(tempFile);
            if(resizedImage != null) resizedImage.compress(Bitmap.CompressFormat.JPEG,100,out);
            else image.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.close();
        } catch (FileNotFoundException e) {
            Log.e("MyTag", "FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("MyTag", "IOException : " + e.getMessage());
        } finally {
            // 크기 조정한 이미지 메모리에서 제거
            if(resizedImage != null) resizedImage.recycle();
        }
    }
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
    @Override
    @WorkerThread
    @Nullable
    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
        try {
            if (mModule == null) {
                mModule = LiteModuleLoader.load(assetFilePath(getApplicationContext(), "50v50508.torchscript.ptl"));
            }
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            return null;
        }
        Bitmap bitmap = imgToBitmap(image.getImage());
        Matrix matrix = new Matrix();
        matrix.postRotate(90.0f);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);

        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        // forward를 통해 객체 탐지 수행
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();

        float imgScaleX = (float)bitmap.getWidth() / PrePostProcessor.mInputWidth;
        float imgScaleY = (float)bitmap.getHeight() / PrePostProcessor.mInputHeight;
        float ivScaleX = (float)mResultView.getWidth() / bitmap.getWidth();
        float ivScaleY = (float)mResultView.getHeight() / bitmap.getHeight();

        final ArrayList<Result> results = PrePostProcessor.outputsToNMSPredictions(outputs, imgScaleX, imgScaleY, ivScaleX, ivScaleY, 0, 0);
        return new AnalysisResult(results);
    }
}
