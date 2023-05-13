// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package org.pytorch.demo.objectdetection;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;

public abstract class AbstractCameraXActivity<R> extends BaseModuleActivity {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 200;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA};

    private long mLastAnalysisResultTime;
    private long captureTime;
    protected int viewWidth = 0;
    protected int viewHeight = 0;
    protected SharedPreferences sharedPreferences;

    // 상속 받은 자식이 원하는 view로 출력하도록 설정
    protected abstract int getContentViewLayoutId();

    // 상속 받은 자식이 원하는 preview 설정
    protected abstract TextureView getCameraPreviewTextureView();

    protected abstract long saveImageToJpeg(Bitmap image, long time, R result);

    protected abstract void sendData(R result);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutId());
        sharedPreferences = getSharedPreferences("settingOption",MODE_PRIVATE);
        startBackgroundThread();
        //권한이 없으면 권한 요청 있으면 setupCameraX()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                REQUEST_CODE_CAMERA_PERMISSION);
        } else {
            setupCameraX();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                    this,
                    "You can't use object detection example without granting CAMERA permission",
                    Toast.LENGTH_LONG)
                    .show();
                finish();
            } else {
                setupCameraX();
            }
        }
    }

    private void setupCameraX() {
        final TextureView textureView = getCameraPreviewTextureView();
        final PreviewConfig previewConfig = new PreviewConfig.Builder().build();
        final Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(output -> textureView.setSurfaceTexture(output.getSurfaceTexture()));

        final ImageAnalysisConfig imageAnalysisConfig =
            new ImageAnalysisConfig.Builder()
                .setTargetResolution(new Size(480, 640))
                .setCallbackHandler(mBackgroundHandler)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();
        final ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
        imageAnalysis.setAnalyzer((image, rotationDegrees) -> {
            if (viewWidth == 0 && viewHeight == 0) {
                Log.d("MyTag", "init view size");
                viewWidth = textureView.getRight();
                viewHeight = textureView.getBottom();
            }
            try {
                Long option_odTime = sharedPreferences.getLong("odTime",SettingOption.odTime);
                Long option_captureTime = sharedPreferences.getLong("captureTime",SettingOption.captureTime);
                if (SystemClock.elapsedRealtime() - mLastAnalysisResultTime < option_odTime) {
                    return;
                }
                final R result = analyzeImage(image, rotationDegrees);
                if (result != null) {
                    // 카메라 캡쳐 5분마다
                    if (SystemClock.elapsedRealtime() - captureTime > option_captureTime) {
                        captureTime = saveImageToJpeg(textureView.getBitmap(), SystemClock.elapsedRealtime(), result);
                    }
                    mLastAnalysisResultTime = SystemClock.elapsedRealtime();
                    runOnUiThread(() -> applyToUiAnalyzeImageResult(result));
                }
            } catch (IllegalStateException e) {
                Log.e("MyTag", e.getMessage());
                return;
            }
        });

        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }

    @WorkerThread
    @Nullable
    protected abstract R analyzeImage(ImageProxy image, int rotationDegrees);

    @UiThread
    protected abstract void applyToUiAnalyzeImageResult(R result);
}
