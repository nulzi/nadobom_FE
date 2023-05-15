package org.pytorch.demo.objectdetection;

import androidx.annotation.Dimension;
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
    private TextView tvTextSize;
    private TextView tvSpeechSpeed;
    private TextView tvHelpOption;
    private TextView tvAppVersion;
    private Button sbtnSmallText;
    private Button sbtnMidText;
    private Button sbtnBigText;
    private Button stbnSlowSpeed;
    private Button sbtnNormalSpeed;
    private Button sbtnFast1Speed;
    private Button sbtnFast2Speed;
    private Button sbtnFast3Speed;
    private Button sbtnFast4Speed;
    private Button sbtnHelpOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences("settingOption",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        textToSpeech = new TextToSpeech(this,this);

        tvTextSize = findViewById(R.id.tv_text_size);
        tvSpeechSpeed = findViewById(R.id.tv_speech_speed);
        tvHelpOption = findViewById(R.id.tv_help_option);
        tvAppVersion = findViewById(R.id.tv_app_version);
        tvAppVersion.setText(tvAppVersion.getText()+"  v"+BuildConfig.VERSION_NAME);

        sbtnSmallText = findViewById(R.id.setbtn_small_text);
        sbtnSmallText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("textSize",28);
                editor.commit();
                tvAppVersion.setTextSize(Dimension.SP,28);
                tvTextSize.setTextSize(Dimension.SP,28);
                tvSpeechSpeed.setTextSize(Dimension.SP,28);
                tvHelpOption.setTextSize(Dimension.SP,28);
                stbnSlowSpeed.setTextSize(Dimension.SP,28);
                sbtnNormalSpeed.setTextSize(Dimension.SP,28);
                sbtnFast1Speed.setTextSize(Dimension.SP,28);
                sbtnFast2Speed.setTextSize(Dimension.SP,28);
                sbtnFast3Speed.setTextSize(Dimension.SP,28);
                sbtnFast4Speed.setTextSize(Dimension.SP,28);
                sbtnHelpOption.setTextSize(Dimension.SP,28);
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak("작은 글씨 크기로 설정합니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        sbtnMidText = findViewById(R.id.setbtn_mid_text);
        sbtnMidText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("textSize",32);
                editor.commit();
                tvAppVersion.setTextSize(Dimension.SP,32);
                tvTextSize.setTextSize(Dimension.SP,32);
                tvSpeechSpeed.setTextSize(Dimension.SP,32);
                tvHelpOption.setTextSize(Dimension.SP,32);
                stbnSlowSpeed.setTextSize(Dimension.SP,32);
                sbtnNormalSpeed.setTextSize(Dimension.SP,32);
                sbtnFast1Speed.setTextSize(Dimension.SP,32);
                sbtnFast2Speed.setTextSize(Dimension.SP,32);
                sbtnFast3Speed.setTextSize(Dimension.SP,32);
                sbtnFast4Speed.setTextSize(Dimension.SP,32);
                sbtnHelpOption.setTextSize(Dimension.SP,32);
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak("보통 글씨 크기로 설정합니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        sbtnBigText = findViewById(R.id.setbtn_big_text);
        sbtnBigText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("textSize",40);
                editor.commit();
                tvAppVersion.setTextSize(Dimension.SP,40);
                tvTextSize.setTextSize(Dimension.SP,40);
                tvSpeechSpeed.setTextSize(Dimension.SP,40);
                tvHelpOption.setTextSize(Dimension.SP,40);
                stbnSlowSpeed.setTextSize(Dimension.SP,40);
                sbtnNormalSpeed.setTextSize(Dimension.SP,40);
                sbtnFast1Speed.setTextSize(Dimension.SP,40);
                sbtnFast2Speed.setTextSize(Dimension.SP,40);
                sbtnFast3Speed.setTextSize(Dimension.SP,40);
                sbtnFast4Speed.setTextSize(Dimension.SP,40);
                sbtnHelpOption.setTextSize(Dimension.SP,40);
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.speak("큰 글씨 크기로 설정합니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        stbnSlowSpeed = findViewById(R.id.setbtn_slow);
        stbnSlowSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",0.8f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(0.8f);
                textToSpeech.speak("말하는 속도가 0.8배 느려집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        sbtnNormalSpeed = findViewById(R.id.setbtn_normal);
        sbtnNormalSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",1.0f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(1.0f);
                textToSpeech.speak("기본 말하기 속도입니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
       sbtnFast1Speed = findViewById(R.id.setbtn_fast1);
        sbtnFast1Speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",1.2f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(1.2f);
                textToSpeech.speak("말하는 속도가 1.2배 빨라집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        sbtnFast2Speed = findViewById(R.id.setbtn_fast2);
        sbtnFast2Speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",1.5f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(1.5f);
                textToSpeech.speak("말하는 속도가 1.5배 빨라집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        sbtnFast3Speed = findViewById(R.id.setbtn_fast3);
        sbtnFast3Speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",1.8f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(1.8f);
                textToSpeech.speak("말하는 속도가 1.8배 빨라집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        sbtnFast4Speed = findViewById(R.id.setbtn_fast4);
        sbtnFast4Speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putFloat("speechSpeed",2.0f);
                editor.commit();
                if(textToSpeech.isSpeaking()) textToSpeech.stop();
                textToSpeech.setSpeechRate(2.0f);
                textToSpeech.speak("말하는 속도가 2배 빨라집니다",TextToSpeech.QUEUE_FLUSH,null,"setSpeechSpeed");
            }
        });
        sbtnHelpOption = findViewById(R.id.setbtn_help_option);
        Boolean option_helpOption = sharedPreferences.getBoolean("helpOption",SettingOption.helpOption);
        if(option_helpOption)sbtnHelpOption.setText("끄기");
        else sbtnHelpOption.setText("켜기");
        sbtnHelpOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sbtnHelpOption.getText().equals("켜기")) {
                    editor.putBoolean("helpOption",true);
                    editor.commit();
                    sbtnHelpOption.setText("끄기");
                    if (textToSpeech.isSpeaking()) textToSpeech.stop();
                    textToSpeech.setSpeechRate(sharedPreferences.getFloat("speechSpeed", SettingOption.speechSpeed));
                    textToSpeech.speak("도움말 켜기", TextToSpeech.QUEUE_FLUSH, null, "setSpeechSpeed");
                } else {
                    editor.putBoolean("helpOption",false);
                    editor.commit();
                    sbtnHelpOption.setText("켜기");
                    if (textToSpeech.isSpeaking()) textToSpeech.stop();
                    textToSpeech.setSpeechRate(sharedPreferences.getFloat("speechSpeed", SettingOption.speechSpeed));
                    textToSpeech.speak("도움말 끄기", TextToSpeech.QUEUE_FLUSH, null, "setSpeechSpeed");
                }
            }
        });

        int option_textSize = sharedPreferences.getInt("textSize",SettingOption.textSize);
        tvAppVersion.setTextSize(Dimension.SP,option_textSize);
        tvTextSize.setTextSize(Dimension.SP,option_textSize);
        tvSpeechSpeed.setTextSize(Dimension.SP,option_textSize);
        tvHelpOption.setTextSize(Dimension.SP,option_textSize);
        stbnSlowSpeed.setTextSize(Dimension.SP,option_textSize);
        sbtnNormalSpeed.setTextSize(Dimension.SP,option_textSize);
        sbtnFast1Speed.setTextSize(Dimension.SP,option_textSize);
        sbtnFast2Speed.setTextSize(Dimension.SP,option_textSize);
        sbtnFast3Speed.setTextSize(Dimension.SP,option_textSize);
        sbtnFast4Speed.setTextSize(Dimension.SP,option_textSize);
        sbtnHelpOption.setTextSize(Dimension.SP,option_textSize);
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