package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by youni on 2016/11/9.
 */

public class ActivityIncomingCall extends ActivityCallBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inoming_call);

        TextView callerView = (TextView) findViewById(R.id.tv_call_caller);
        callerView.setText(MediaCallManager.getInstance().getIncommingCaller() + " is calling");

        MediaCallManager.getInstance().addStateListener(_callStateListener);

        Button answer = (Button) findViewById(R.id.btn_call_answer);
        Button reject = (Button) findViewById(R.id.btn_call_reject);

        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MediaCallManager.getInstance().answerCall();

                    startActivity(new Intent(ActivityIncomingCall.this,ActivityAcceptCall.class));

                    finish();
                } catch (Exception e) {
                    e.printStackTrace();

                    finish();
                }
            }
        });


        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MediaCallManager.getInstance().hangupCall();
                } catch (Exception e) {
                    e.printStackTrace();

                    finish();
                }
            }
        });

        playRingtong();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MediaCallManager.getInstance().removeStateListener(_callStateListener);
    }

    private MediaCallManager.OnCallStateChangeListener _callStateListener = new MediaCallManager.OnCallStateChangeListener() {
        @Override
        public void OnCallStateChanged(MediaCallManager.CallState state) {
            if (state == MediaCallManager.CallState.EHangup || state == MediaCallManager.CallState.EReject){
                finish();
            } else if (state == MediaCallManager.CallState.EAccepted){
                startActivity(new Intent(ActivityIncomingCall.this,ActivityAcceptCall.class));

                finish();
            }
        }
    };

    void playRingtong(){
        Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        _audioManager.setMode(AudioManager.MODE_RINGTONE);
        _audioManager.setSpeakerphoneOn(true);
        _ringtone = RingtoneManager.getRingtone(this, ringUri);
        _ringtone.play();
    }

}
