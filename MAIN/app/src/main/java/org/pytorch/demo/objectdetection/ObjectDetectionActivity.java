package org.pytorch.demo.objectdetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
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

// 이와 같이 <타입>이 가능한 이유는 해당 클래스가 static 클래스이기 때문이다.
public class ObjectDetectionActivity extends AbstractCameraXActivity<ObjectDetectionActivity.AnalysisResult> {
    private Module mModule = null;
    // 객체 탐지 결과 화면
    private ResultView mResultView;
    private TextView mLiveText;
    private TextToSpeech textToSpeech;
    private ImageView imageView;
    private int deviceWidth;
    private int deviceHeight;

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
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        deviceWidth = size.x;
        deviceHeight = size.y;
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
        final Button endButton = findViewById(R.id.endButton);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    @Override
    protected void onDestroy() {
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
//        Log.e("MyTag","ods size : " + ods.size());
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
//        Log.d("MyTag_normalization","size: "+viewWidth+", "+viewHeight);
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
                Log.e("MyTag","delete complete" + path);
            } else {
                // 파일 삭제 실패
                Log.e("MyTag","delete fail" + path);
            }
        } else {
            // 파일이 존재하지 않음
            Log.e("MyTag","file not exist" + path);
        }
    }

    private String makeResultText(ArrayList<String> results) {
        if (results.size() == 2) return results.get(0) + " " + results.get(1);
        String location1 = results.get(0);
        String result1 = results.get(1);
        String location2 = results.get(2);
        String result2 = results.get(3);
//        String count = results.get(4);
        Log.d("MyTag", location1 + " " + result1 + ", " + location2 + " " + result2);
        if (location1.equals(location2) && result1.equals(result2))
            return location1 + " " + result1 + " 2개";
        if (location1.equals(location2)) return location1 + " " + result1 + ", 그리고 " + result2;
        return location1 + " " + result1 + ", " + location2 + " " + result2;
    }

    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
        mResultView.setResults(result.mResults);
        mResultView.invalidate();
        if (!result.mResults.isEmpty())
            mLiveText.setText(makeResultText(Priority.priority(Priority.input(result.mResults), viewWidth, viewHeight)));
        else mLiveText.setText("장애물 탐색 중");
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
        if (result.mResults.size() <= 0) return time;
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

    @Override
    @WorkerThread
    @Nullable
    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
        try {
            if (mModule == null) {
                mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "50v50425.torchscript.ptl"));
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
