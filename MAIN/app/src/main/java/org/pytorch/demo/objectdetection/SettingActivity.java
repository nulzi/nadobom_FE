package org.pytorch.demo.objectdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences("settingOption",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        textToSpeech = new TextToSpeech(this,this);

        final TextView appVersionText = findViewById(R.id.appVersionText);
        appVersionText.setText(appVersionText.getText()+"  v"+BuildConfig.VERSION_NAME);

        final Button smallTextSbtn = findViewById(R.id.smallTextSbtn);
        smallTextSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("textSize",28);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak("작은 글씨 크기로 설정합니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button midTextSbtn = findViewById(R.id.midTextSbtn);
        midTextSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("textSize",32);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak("보통 글씨 크기로 설정합니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button bigTextSbtn = findViewById(R.id.bigTextSbtn);
        bigTextSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("textSize",40);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak("큰 글씨 크기로 설정합니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button slowSpeedSbtn = findViewById(R.id.slowSpeedSbtn);
        slowSpeedSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",0.8f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(0.8f);
                textToSpeech.speak("말하는 속도가 0.8배 느려집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button normalSpeedSbtn = findViewById(R.id.normalSpeedSbtn);
        normalSpeedSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",1.0f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(1.0f);
                textToSpeech.speak("기본 말하기 속도입니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button fast1SpeedSbtn = findViewById(R.id.fast1SpeedSbtn);
        fast1SpeedSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",1.2f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(1.2f);
                textToSpeech.speak("말하는 속도가 1.2배 빨라집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button fast2SpeedSbtn = findViewById(R.id.fast2SpeedSbtn);
        fast2SpeedSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",1.5f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(1.5f);
                textToSpeech.speak("말하는 속도가 1.5배 빨라집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button fast3SpeedSbtn = findViewById(R.id.fast3SpeedSbtn);
        fast3SpeedSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",1.8f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(1.8f);
                textToSpeech.speak("말하는 속도가 1.8배 빨라집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button fast4SpeedSbtn = findViewById(R.id.fast4SpeedSbtn);
        fast4SpeedSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",2.0f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(2.0f);
                textToSpeech.speak("말하는 속도가 2배 빨라집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        final Button helpSbtn = findViewById(R.id.helpSbtn);
        Boolean option_helpOption = sharedPreferences.getBoolean("helpOption",SettingOption.helpOption);
        if(option_helpOption)helpSbtn.setText("끄기");
        else helpSbtn.setText("켜기");
        helpSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(helpSbtn.getText().equals("켜기")) {
                    editor.putBoolean("helpOption",true);
                    editor.commit();
                    helpSbtn.setText("끄기");
                    if (textToSpeech.isSpeaking()) textToSpeech.stop();
                    textToSpeech.setSpeechRate(sharedPreferences.getFloat("speechSpeed", SettingOption.speechSpeed));
                    textToSpeech.speak("도움말 켜기", TextToSpeech.QUEUE_FLUSH, null, "setSpeechSpeed");
                } else {
                    editor.putBoolean("helpOption",false);
                    editor.commit();
                    helpSbtn.setText("켜기");
                    if (textToSpeech.isSpeaking()) textToSpeech.stop();
                    textToSpeech.setSpeechRate(sharedPreferences.getFloat("speechSpeed", SettingOption.speechSpeed));
                    textToSpeech.speak("도움말 끄기", TextToSpeech.QUEUE_FLUSH, null, "setSpeechSpeed");
                }
            }
        });
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            Float option_speechSpeed = sharedPreferences.getFloat("speechSpeed",SettingOption.speechSpeed);
            textToSpeech.setLanguage(Locale.KOREAN);
            textToSpeech.setPitch(1.0f);
            textToSpeech.setSpeechRate(option_speechSpeed);
            textToSpeech.speak("설정 화면입니다. 원하시는대로 설정을 변경하세요.",TextToSpeech.QUEUE_FLUSH,null,"settingComment");
        }
        else Log.e("MyTag","TTS initialization fail");
    }

    @Override
    protected void onDestroy() {
        if(textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}