// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package org.pytorch.demo.objectdetection;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public static TextToSpeech textToSpeech;
    private SharedPreferences sharedPreferences;
    private Button btnHelp;
    private Button btnLive;
    private Button btnSetting;
    private boolean option_help;
    private float option_speechSpeed;
    private int option_textSize;
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
        textToSpeech = new TextToSpeech(this, this);
        btnHelp = findViewById(R.id.btn_help_start);
        option_help = sharedPreferences.getBoolean("helpOption",SettingOption.helpOption);
        if (!option_help) btnHelp.setVisibility(View.INVISIBLE);
        else {
            btnHelp.setVisibility(View.VISIBLE);
        }
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak("다음은 메인 화면 도움말입니다 장애물 탐지 시작버튼, 설정을 위한 설정버튼이 있습니다", TextToSpeech.QUEUE_ADD, null, "helpComment");
            }
        });

        btnLive = findViewById(R.id.btn_live);
        btnLive.setTextSize(Dimension.SP,option_textSize);
        btnLive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (textToSpeech != null && textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
              final Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
              startActivity(intent);
            }
        });

        btnSetting = findViewById(R.id.btn_setting);
        btnSetting.setTextSize(Dimension.SP,option_textSize);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textToSpeech != null && textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                final Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
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
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            option_speechSpeed = sharedPreferences.getFloat("speechSpeed",SettingOption.speechSpeed);
            option_help = sharedPreferences.getBoolean("helpOption",SettingOption.helpOption);
            textToSpeech.setLanguage(Locale.KOREAN);
            textToSpeech.setPitch(1.0f);
            textToSpeech.setSpeechRate(option_speechSpeed);
            textToSpeech.speak("반갑습니다 나도봄 시작 화면입니다.", TextToSpeech.QUEUE_FLUSH, null, "startComment");
            if (option_help) textToSpeech.speak("다음은 메인 화면 도움말입니다 장애물 탐지 시작버튼, 설정을 위한 설정버튼이 있습니다", TextToSpeech.QUEUE_ADD, null, "helpComment");
        } else Log.e("MyTag", "TTS initialization fail");
    }

    @Override
    protected void onResume() {
        // 다시 화면으로 돌아올 때 동작
        if (textToSpeech != null) {
            option_help = sharedPreferences.getBoolean("helpOption",SettingOption.helpOption);
            option_speechSpeed = sharedPreferences.getFloat("speechSpeed",SettingOption.speechSpeed);
            option_textSize = sharedPreferences.getInt("textSize",SettingOption.textSize);

            if (!option_help) btnHelp.setVisibility(View.INVISIBLE);
            else btnHelp.setVisibility(View.VISIBLE);

            btnLive.setTextSize(Dimension.SP,option_textSize);
            btnSetting.setTextSize(Dimension.SP,option_textSize);

            textToSpeech.setSpeechRate(option_speechSpeed);
            textToSpeech.speak("반갑습니다. 나도봄 시작 화면입니다.", TextToSpeech.QUEUE_FLUSH, null, "startComment");
        }
        else Log.e("MyTag","textToSpeech is null");
        super.onResume();
    }
}
