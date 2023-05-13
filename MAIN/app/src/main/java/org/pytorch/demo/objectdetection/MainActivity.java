// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package org.pytorch.demo.objectdetection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable {

    private AlertDialog helpDialog;
    private SharedPreferences sharedPreferences;
    private Button helpButton;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("settingOption",MODE_PRIVATE);
        // 설정 옵션 초기화
        if(sharedPreferences.getAll().size() == 0){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("speechSpeed",SettingOption.speechSpeed);
            editor.putLong("odTime",SettingOption.odTime);
            editor.putLong("captureTime",SettingOption.captureTime);
            editor.putInt("textSize",SettingOption.textSize);
            editor.putBoolean("helpOption",SettingOption.helpOption);
            editor.commit();
        }
        helpButton = findViewById(R.id.helpButton);
        Boolean helpOption = sharedPreferences.getBoolean("helpOption",true);
        if (!helpOption) helpButton.setVisibility(View.INVISIBLE);
        else {
            helpButton.setVisibility(View.VISIBLE);
            showHelpDialog();
        }
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });

        final Button buttonLive = findViewById(R.id.liveButton);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              final Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
              startActivity(intent);
            }
        });
            public void onClick(View v) {
            }
        });

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    @Override
    }

    @Override
    private void showHelpDialog() {
        if(textToSpeech.isSpeaking()) textToSpeech.stop();
        Float option_speechSpeed = sharedPreferences.getFloat("speechSpeed",SettingOption.speechSpeed);
        final View view = LayoutInflater.from(this).inflate(R.layout.help_dialog, findViewById(R.id.helpDialog));
        final AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(view);
        TextView helpView = view.findViewById(R.id.helpText);
        helpView.setText("다음은 메인 화면 도움말입니다 장애물 탐지 시작버튼, 길이 불편할 때 신고할 수 있는 신고버튼, 설정을 위한 설정버튼이 있습니다");
        textToSpeech.setSpeechRate(option_speechSpeed);
        textToSpeech.speak(helpView.getText(), TextToSpeech.QUEUE_ADD, null, "helpComment");

        if(helpDialog == null) helpDialog = builder.create();

        if(!helpDialog.isShowing()) helpDialog.show();
        view.findViewById(R.id.replayBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak(helpView.getText(), TextToSpeech.QUEUE_FLUSH, null, "helpComment");
            }
        });
        view.findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.stop();
                helpDialog.dismiss();
            }
        });
    }
}
