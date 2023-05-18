package org.pytorch.demo.objectdetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
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
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.camera.core.ImageProxy;

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
import java.util.Locale;

// 이와 같이 <타입>이 가능한 이유는 해당 클래스가 static 클래스이기 때문이다.
public class ObjectDetectionActivity extends AbstractCameraXActivity<ObjectDetectionActivity.AnalysisResult> implements TextToSpeech.OnInitListener {
    private Module mModule = null;
    // 객체 탐지 결과 화면
    private ResultView mResultView;
    private TextToSpeech textToSpeech;
    private TextView tvObstacle;
    private Button btnHelp;
    private Button btnReport;
    private Button btnEnd;
    private boolean option_help;

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

        option_help = sharedPreferences.getBoolean("helpOption",SettingOption.helpOption);
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
        tvObstacle.setTextSize(Dimension.SP, sharedPreferences.getInt("textSize",SettingOption.textSize));
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
        btnReport.setTextSize(Dimension.SP, sharedPreferences.getInt("textSize",SettingOption.textSize));
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            textToSpeech.speak("장애물 탐색을 시작하겠습니다.", TextToSpeech.QUEUE_FLUSH, null, "mainComment");
            if (option_help) textToSpeech.speak("다음은 장애물 탐지 화면 도움말입니다 장애물 안내텍스트, 신고를 위한 신고 버튼, 장애물 탐지 종료버튼이 있습니다", TextToSpeech.QUEUE_ADD, null, "helpComment");
        } else Log.e("MyTag", "TTS initialization fail");
    }
    @Override
    protected void onDestroy() {
        textToSpeech.stop();
        resetCacheDir();
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

    private void resetCacheDir() {
        File file = new File(getCacheDir().toString());

        File[] files = file.listFiles();

        String path = getCacheDir() + "/";
        for (File tempFile : files) {
            if (tempFile.getName().contains("_od")) {
                deleteImg(path + tempFile.getName());
            }
        }
    }

    @Nullable
    private File getFileFromCacheDir(){
        ArrayList<String> ods = new ArrayList<>();

        File file = new File(getCacheDir().toString());

        File[] files = file.listFiles();

        for(File tempFile : files){
            if(tempFile.getName().contains("_od")){
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
    protected void sendData(AnalysisResult result) {
        if (result.mResults.size() == 0) return;
        File file = getFileFromCacheDir();
        ArrayList<String> resultList = normalization(result.mResults, viewWidth, viewHeight);
        if (file != null) API.obstacleDataTransfer(file, resultList);
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
    @Override
    protected long saveImageToJpeg(Bitmap image, long time, AnalysisResult result) {
        if (result.mResults.size() <= 0 || image == null) return time;
        File storage = getCacheDir();
        String timeString = Long.toString(time);
        String fileName = timeString + "_od.jpg";

        File tempFile = new File(storage, fileName);

        try {
            tempFile.createNewFile();

            FileOutputStream out = new FileOutputStream(tempFile);

            image.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.close();
        } catch (FileNotFoundException e) {
            Log.e("MyTag", "FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("MyTag", "IOException : " + e.getMessage());
        }
        sendData(result);
        return SystemClock.elapsedRealtime();
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
