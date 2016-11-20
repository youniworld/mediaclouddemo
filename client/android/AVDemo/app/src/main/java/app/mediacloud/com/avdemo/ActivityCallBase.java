package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by youni on 2016/11/9.
 */

public class ActivityCallBase extends Activity {
    AudioManager _audioManager;
    SoundPool _soundPool;
    int _outgoing;
    Ringtone _ringtone;

    protected ExecutorService _callExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onBackPressed() {
        _callExecutor.shutdownNow();
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }

    // 打开扬声器
    protected void openSpeakerOn() {
        try {
            if (!_audioManager.isSpeakerphoneOn()){
                _audioManager.setSpeakerphoneOn(true);
            }

            _audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 关闭扬声器
    protected void closeSpeakerOn() {

        try {
            if (_audioManager != null) {
                // int curVolume =
                // audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                if (_audioManager.isSpeakerphoneOn()){
                    _audioManager.setSpeakerphoneOn(false);
                }

                _audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                // curVolume, AudioManager.STREAM_VOICE_CALL);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void playMakeCallSounds() {
        try {
            if(_soundPool == null){
                _soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
                _outgoing = _soundPool.load(this, R.raw.outgoing, 1);
            }

            _audioManager.setMode(AudioManager.MODE_RINGTONE);
            _audioManager.setSpeakerphoneOn(true);

            _soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    int id = _soundPool.play(_outgoing, // 声音资源
                            0.3f, // 左声道
                            0.3f, // 右声道
                            1, // 优先级，0最低
                            -1, // 循环次数，0是不循环，-1是永远循环
                            1); // 回放速度，0.5-2.0之间。1为正常速度
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            Log.i("call",e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (_soundPool != null)
            _soundPool.release();

        if (_ringtone != null && _ringtone.isPlaying())
            _ringtone.stop();

        _audioManager.setMode(AudioManager.MODE_NORMAL);
        _audioManager.setMicrophoneMute(false);
    }
}
