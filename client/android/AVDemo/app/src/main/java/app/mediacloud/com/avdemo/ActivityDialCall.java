package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ExecutorService;

/**
 * Created by youni on 2016/11/9.
 */

public class ActivityDialCall extends ActivityCallBase {
    private Activity me;
    private String _to;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _to = getIntent().getStringExtra("to");

        if (_to == null){
            Log.e("call","no to target set!!");

            finish();
            return;
        }

        setContentView(R.layout.activity_dial_call);

        me = this;

        MediaCallManager.getInstance().addStateListener(_callStateListener);

        _callExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaCallManager.getInstance().makeCall(_to);
                } catch (Exception e) {
                    e.printStackTrace();

                    finish();
                }
            }
        });

        Button hangup = (Button) findViewById(R.id.btn_call_hangup);

        hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (isDestroyed()){
                        return;
                    }
                }

                if (isFinishing()){
                    return;
                }

                _callExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MediaCallManager.getInstance().hangupCall();
                        } catch (Exception e) {
                            e.printStackTrace();

                            finish();
                        }
                    }
                });
            }
        });
    }

    private MediaCallManager.OnCallStateChangeListener _callStateListener = new MediaCallManager.OnCallStateChangeListener() {
        @Override
        public void OnCallStateChanged(MediaCallManager.CallState state) {
            if (state == MediaCallManager.CallState.EAccepted){
                me.startActivity(new Intent(me,ActivityAcceptCall.class));

                finish();
            } else if (state == MediaCallManager.CallState.EHangup || state == MediaCallManager.CallState.EReject){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if(isDestroyed()){
                        return;
                    }
                }

                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        MediaCallManager.getInstance().removeStateListener(_callStateListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
