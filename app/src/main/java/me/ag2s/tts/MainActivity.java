package me.ag2s.tts;

import static me.ag2s.tts.services.Constants.CUSTOM_VOICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import me.ag2s.tts.data.TtsActor;
import me.ag2s.tts.data.TtsActorManger;
import me.ag2s.tts.databinding.ActivityMainBinding;
import me.ag2s.tts.services.Constants;
import me.ag2s.tts.services.TtsVoiceSample;


public class MainActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "CheckVoiceData";
    private static final AtomicInteger mNextRequestId = new AtomicInteger(0);

    boolean connected = false;
    ActivityMainBinding binding;

    TextToSpeech textToSpeech;
    int volumeValue;
    int rateDelta;
    int pitchDelta;
    //Spinner第一次setSelection时系统会自动回调一次onItemSelected，这个计数器用来跳过那一次，
    //避免刚打开App就自动念一遍示例语音。
    private int voiceSpinnerCallbackCount = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        connectToText2Speech();


        binding.btnSetTts.setOnClickListener(this);
        binding.btnKillBattery.setOnClickListener(this);
        binding.ttsVoiceVolumeAdd.setOnClickListener(this);
        binding.ttsVoiceVolumeReduce.setOnClickListener(this);
        binding.ttsRateDeltaAdd.setOnClickListener(this);
        binding.ttsRateDeltaReduce.setOnClickListener(this);
        binding.ttsPitchDeltaAdd.setOnClickListener(this);
        binding.ttsPitchDeltaReduce.setOnClickListener(this);

        volumeValue = APP.getInt(Constants.VOICE_VOLUME, 100);//sharedPreferences.getInt(Constants.VOICE_VOLUME, 100);
        rateDelta = APP.getInt(Constants.VOICE_RATE_DELTA, 0);
        pitchDelta = APP.getInt(Constants.VOICE_PITCH_DELTA, 0);

        updateView();
        binding.ttsVoiceVolume.setOnSeekBarChangeListener(this);
        binding.ttsRateDelta.setOnSeekBarChangeListener(this);
        binding.ttsPitchDelta.setOnSeekBarChangeListener(this);

        //boolean useCustomVoice = APP.getBoolean(Constants.USE_CUSTOM_VOICE, true);//sharedPreferences.getBoolean(Constants.USE_CUSTOM_VOICE, true);
        binding.switchUseCustomVoice.setChecked(APP.getBoolean(Constants.USE_CUSTOM_VOICE, true));
        binding.switchUseCustomVoice.setOnCheckedChangeListener((buttonView, isChecked) -> APP.putBoolean(Constants.USE_CUSTOM_VOICE, isChecked));

        //自定义服务器：地址回显 + 保存按钮（现在是唯一的合成方式，不再需要开关）
        binding.etCustomServerUrl.setText(APP.getString(Constants.CUSTOM_SERVER_URL, ""));
        binding.btnSaveCustomServer.setOnClickListener(this);


        List<TtsActor> voiceActors = TtsActorManger.getInstance().getActors();
        List<String> voiceDisplayNames = new ArrayList<>();
        for (TtsActor actor : voiceActors) {
            voiceDisplayNames.add(actor.getLocale().getDisplayLanguage(Locale.getDefault()) + " · " + actor.getNote() + "  (" + actor.getShortName() + ")");
        }
        ArrayAdapter<String> voiceSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, voiceDisplayNames);
        voiceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVoiceActors.setAdapter(voiceSpinnerAdapter);
        binding.spinnerVoiceActors.setSelection(APP.getInt(Constants.CUSTOM_VOICE_INDEX, 0), false);
        binding.spinnerVoiceActors.setOnItemSelectedListener(this);


    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //跳过Spinner设置初始选中项时系统自动触发的那一次回调，只处理用户真正选择的情况
        if (voiceSpinnerCallbackCount++ == 0) {
            return;
        }
        List<TtsActor> voiceActors = TtsActorManger.getInstance().getActors();
        if (position < 0 || position >= voiceActors.size()) {
            return;
        }
        TtsActor item = voiceActors.get(position);

        boolean origin = APP.getBoolean(Constants.USE_CUSTOM_VOICE, true);
        if (origin) {
            APP.putString(CUSTOM_VOICE, item.getShortName());
            APP.putInt(Constants.CUSTOM_VOICE_INDEX, position);
        }

        Locale locale = item.getLocale();

        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            connectToText2Speech();
            Bundle bundle = new Bundle();
            bundle.putString(CUSTOM_VOICE, item.getShortName());
            bundle.putInt(Constants.CUSTOM_VOICE_INDEX, position);
            bundle.putString("voiceName", item.getShortName());
            bundle.putString("language", locale.getISO3Language());
            bundle.putString("country", locale.getISO3Country());
            bundle.putString("variant", item.getGender() ? "Female" : "Male");
            bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Sample");
            textToSpeech.speak(TtsVoiceSample.getByLocate(this, locale), TextToSpeech.QUEUE_FLUSH, bundle, MainActivity.class.getName() + mNextRequestId.getAndIncrement());
        } else {
            if (textToSpeech == null) {
                connectToText2Speech();
            }
            Toast.makeText(MainActivity.this, item.getShortName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    /**
     * 连接Text2Speech
     */
    private void connectToText2Speech() {
        if (textToSpeech == null || textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null, null) != TextToSpeech.SUCCESS) {
            textToSpeech = new TextToSpeech(MainActivity.this, status -> {

                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result != TextToSpeech.LANG_MISSING_DATA
                            && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        connected = true;
                        if (!textToSpeech.isSpeaking()) {
                            textToSpeech.speak("初始化成功。", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            }, this.getPackageName());
        }

    }


    @SuppressLint("SetTextI18n")
    private void updateView() {
        APP.putInt(Constants.VOICE_VOLUME, volumeValue);
        APP.putInt(Constants.VOICE_RATE_DELTA, rateDelta);
        APP.putInt(Constants.VOICE_PITCH_DELTA, pitchDelta);

        //同步滑块位置——之前点加减按钮只改了数值和文字，滑块本身没有跟着动
        binding.ttsVoiceVolume.setProgress(volumeValue);
        binding.ttsRateDelta.setProgress(rateDelta);
        binding.ttsPitchDelta.setProgress(pitchDelta);

        binding.ttsVolumeLabel.setText(String.format(Locale.US, "音量: %d", volumeValue));
        binding.ttsRateLabel.setText(String.format(Locale.US, "语速: %+d%%", rateDelta));
        binding.ttsPitchLabel.setText(String.format(Locale.US, "音调: %+d%%", pitchDelta));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean i = powerManager.isIgnoringBatteryOptimizations(this.getPackageName());
            if (i) {
                binding.btnKillBattery.setVisibility(View.GONE);
            } else {
                binding.btnKillBattery.setVisibility(View.VISIBLE);
            }
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, R.string.battery_optimizations);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == Menu.FIRST + 2) {
            killBATTERY();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setTTS() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setAction("com.android.settings.TTS_SETTINGS");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @SuppressLint("BatteryLife")
    private void killBATTERY() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm.isIgnoringBatteryOptimizations(packageName))
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == binding.btnSetTts.getId()) {
            setTTS();
        } else if (id == binding.btnKillBattery.getId()) {
            killBATTERY();
        } else if (id == binding.ttsVoiceVolumeReduce.getId()) {
            if (volumeValue > 1) {
                volumeValue--;
                updateView();
            }
        } else if (id == binding.ttsVoiceVolumeAdd.getId()) {
            if (volumeValue < 100) {
                volumeValue++;
                updateView();
            }
        } else if (id == binding.ttsRateDeltaReduce.getId()) {
            if (rateDelta > -50) {
                rateDelta--;
                updateView();
            }
        } else if (id == binding.ttsRateDeltaAdd.getId()) {
            if (rateDelta < 100) {
                rateDelta++;
                updateView();
            }
        } else if (id == binding.ttsPitchDeltaReduce.getId()) {
            if (pitchDelta > -50) {
                pitchDelta--;
                updateView();
            }
        } else if (id == binding.ttsPitchDeltaAdd.getId()) {
            if (pitchDelta < 50) {
                pitchDelta++;
                updateView();
            }
        } else if (id == binding.btnSaveCustomServer.getId()) {
            String url = binding.etCustomServerUrl.getText().toString().trim();
            APP.putString(Constants.CUSTOM_SERVER_URL, url);
            Toast.makeText(MainActivity.this, R.string.custom_server_url_saved, Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        if (id == binding.ttsVoiceVolume.getId()) {
            volumeValue = progress;
            updateView();
        } else if (id == binding.ttsRateDelta.getId()) {
            rateDelta = progress;
            updateView();
        } else if (id == binding.ttsPitchDelta.getId()) {
            pitchDelta = progress;
            updateView();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}