package org.pytorch.demo.objectdetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

// 이와 같이 <타입>이 가능한 이유는 해당 클래스가 static 클래스이기 때문이다.
public class ObjectDetectionActivity extends AbstractCameraXActivity<ObjectDetectionActivity.AnalysisResult> {
    private Module mModule = null;
    // 객체 탐지 결과 화면
    private ResultView mResultView;
    private TextView mLiveText;
    private TextToSpeech textToSpeech;
    private ImageView imageView;

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
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.KOREAN);
                }
            }
        });
        mLiveText = findViewById(R.id.liveText);
        mLiveText.setText("장애물 탐색 중");
        mLiveText.addTextChangedListener(new TextWatcher() {
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
                    textToSpeech.setPitch(1.0f);
                    textToSpeech.setSpeechRate(1.0f);
                    textToSpeech.speak(editable.toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
        imageView = findViewById(R.id.imageView);
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
    @Nullable
    private Bitmap getBitmapFromCacheDir(){
        ArrayList<String> ods = new ArrayList<>();

        File file = new File(getCacheDir().toString());

        File[] files = file.listFiles();

        for(File tempFile : files){
            if(tempFile.getName().contains("_od")){
                ods.add(tempFile.getName());
            }
        }

        Log.e("MyTag","ods size : " + ods.size());
        String path = getCacheDir() + "/";
        if(ods.size() > 1){
            deleteImg(path+ods.get(0));
            ods.remove(0);
        }
        if(ods.size() > 0){
            path = path + ods.get(0);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            return bitmap;
        }
        return null;
    }
    private void deleteImg(String path){
        File file = new File(path);
        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (isDeleted) {
                // 파일 삭제 성공
                Log.e("MyTag","delete complete");
            } else {
                // 파일 삭제 실패
                Log.e("MyTag","delete fail");
            }
        } else {
            // 파일이 존재하지 않음
            Log.e("MyTag","file not exist");
        }
    }

    private String makeResultText(ArrayList<Result> results) {
        String location1 = results.get(0).rect.right < 360 ? "좌측: " : results.get(0).rect.right > 780 ? "우측: " : "정면: ";
        String result1 = location1 + PrePostProcessor.mClasses[results.get(0).classIndex];
        String location2 = results.get(1).rect.right < 360 ? "좌측: " : results.get(1).rect.right > 780 ? "우측: " : "정면: ";
        String result2 = location2 + PrePostProcessor.mClasses[results.get(1).classIndex];
        final String resultText = result1 + ", " + result2;
        return resultText;
    }

    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
        mResultView.setResults(result.mResults);
        mResultView.invalidate();
        if(!result.mResults.isEmpty()) mLiveText.setText(makeResultText(result.mResults));
        if(getBitmapFromCacheDir() != null) imageView.setImageBitmap(getBitmapFromCacheDir());
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
    protected void saveImageToJpeg(Bitmap image, long time) {
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
            Log.e("MyTag","FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("MyTag", "IOException : " + e.getMessage());
        }
    }

    @Override
    @WorkerThread
    @Nullable
    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
        try {
            if (mModule == null) {
                mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "v50411.torchscript.ptl"));
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
        if(results.size() == 0) return null;
        return new AnalysisResult(results);
    }
}
