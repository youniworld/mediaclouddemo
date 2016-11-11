package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.vlee78.android.media.MediaSdk;
import com.vlee78.android.media.MediaView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by youni on 2016/11/9.
 */

public class ActivityAcceptCall extends ActivityCallBase{
    private String _mediaSessionId;
    private String _url;
    private String _hpspUrl;
    private boolean _steamClosed = true;
    private ExecutorService _streamExecutor = Executors.newSingleThreadExecutor();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _mediaSessionId = MediaCallManager.getInstance().getMediaSessionId();

        if (_mediaSessionId == null){
            Log.e("call","no _media session id found!!");

            finish();
            return;
        }

        setContentView(R.layout.activity_accept_call);

        MediaCallManager.getInstance().addStateListener(_stateChangeListener);

        Button hangup = (Button) findViewById(R.id.btn_call_hangup);

        hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            closeStream();
                            MediaCallManager.getInstance().hangupCall();
                        } catch (Exception e) {
                            e.printStackTrace();

                            finish();
                        }
                    }
                }).start();
            }
        });


        MediaView view = (MediaView) findViewById(R.id.mv_call_view);
        MediaView preview = (MediaView) findViewById(R.id.mv_preview);

        view.bind(100);
        preview.bind(101);

        _hpspUrl = "hpsp://"+_mediaSessionId;
        _url = String.format("%s:%s",_hpspUrl,AppModel.getInstance().getUid());

        openStream();
    }

    private MediaCallManager.OnCallStateChangeListener _stateChangeListener = new MediaCallManager.OnCallStateChangeListener() {
        @Override
        public void OnCallStateChanged(MediaCallManager.CallState state) {
            if (state == MediaCallManager.CallState.EHangup || state == MediaCallManager.CallState.EReject){
                closeStream();
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MediaCallManager.getInstance().removeStateListener(_stateChangeListener);
    }

    void openStream(){
        _streamExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MediaSdk.open(6, _url, 100, 101);

                MediaSdk.setCameraFront(true);
                MediaSdk.setPushRecord(true);
                _steamClosed = false;
            }
        });
    }

    void closeStream(){
        if (_steamClosed){
            return;
        }

        _streamExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (_steamClosed){
                    return;
                }

                MediaSdk.close(_hpspUrl);
                _steamClosed = true;
            }
        });
    }
}
