package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by youni on 2016/11/9.
 */

public class ActivityDialCall extends Activity {
    private Activity me;
    private String _to;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dial_call);

        me = this;

        MediaCallManager.getInstance().addStateListener(_callStateListener);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaCallManager.getInstance().makeCall(_to);
                } catch (Exception e) {
                    e.printStackTrace();

                    finish();
                }
            }
        }).start();
    }

    private MediaCallManager.OnCallStateChangeListener _callStateListener = new MediaCallManager.OnCallStateChangeListener() {
        @Override
        public void OnCallStateChanged(MediaCallManager.CallState state) {
            if (state == MediaCallManager.CallState.EAccepted){
                me.startActivity(new Intent(me,ActivityAcceptCall.class));

                finish();
            } else if (state == MediaCallManager.CallState.EHangup || state == MediaCallManager.CallState.EReject){
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaCallManager.getInstance().removeStateListener(_callStateListener);
    }
}
