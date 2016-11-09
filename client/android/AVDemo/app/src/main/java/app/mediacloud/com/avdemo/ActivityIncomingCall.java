package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by youni on 2016/11/9.
 */

public class ActivityIncomingCall extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inoming_call);

        MediaCallManager.getInstance().addStateListener(_callStateListener);

        Button answer = (Button) findViewById(R.id.btn_call_answer);
        Button reject = (Button) findViewById(R.id.btn_call_reject);

        answer.setOnClickListener(new View.OnClickListener() {
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


        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MediaCallManager.getInstance().answerCall();
                } catch (Exception e) {
                    e.printStackTrace();

                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MediaCallManager.getInstance().removeStateListener(_callStateListener);
    }

    private MediaCallManager.OnCallStateChangeListener _callStateListener = new MediaCallManager.OnCallStateChangeListener() {
        @Override
        public void OnCallStateChanged(MediaCallManager.CallState state) {
            if (state == MediaCallManager.CallState.EReject || state == MediaCallManager.CallState.EReject){
                finish();
            } else if (state == MediaCallManager.CallState.EAccepted){
                startActivity(new Intent(ActivityIncomingCall.this,ActivityAcceptCall.class));

                finish();
            }
        }
    };

}
