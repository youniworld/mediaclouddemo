package app.mediacloud.com.avdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mmc.android.sdk.IRealTimeVideo;
import com.mmc.android.sdk.IRealTimeVideoCamera;
import com.mmc.android.sdk.MMCException;
import com.mmc.android.sdk.MMCSDK;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by youni on 2016/11/9.
 */

public class ActivityAcceptCall extends ActivityCallBase implements IRealTimeVideo.IListener {
    private String _mediaSessionId;
    private String _url;
    private String _uid;
    private String _hpspUrl;
    private boolean _steamClosed = true;

    private IRealTimeVideoCamera _camera;
    private IRealTimeVideo _rtVideo;
    private View _callView;

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

        TextView callName = (TextView) findViewById(R.id.tv_call_name);

        callName.setText(MediaCallManager.getInstance().getPeer());

        MediaCallManager.getInstance().addStateListener(_stateChangeListener);

        Button hangup = (Button) findViewById(R.id.btn_call_hangup);

        hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    closeStream();
                    MediaCallManager.getInstance().hangupCall();
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                }
            }
        });

        try {
            _camera = MMCSDK.CreateRealTimeVideoCamera(getWindowManager().getDefaultDisplay(), null);
            _camera.Start();

            FrameLayout frmlayout = (FrameLayout)findViewById(R.id.mv_preview);
            FrameLayout.LayoutParams fparam = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            frmlayout.addView(_camera.GetPreviewView(), fparam);
        }
        catch (MMCException ex) {
            ex.printStackTrace();
        }

        _hpspUrl = "hpsp://if.biz.hpsp.hifun.mobi/" + _mediaSessionId;
        _uid = AppModel.getInstance().getUid();
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
        try {
            if (_camera != null) {
                _camera.Stop();
            }

            if (_rtVideo != null) {
                _rtVideo.Stop();
            }
        }
        catch (MMCException ex) {
            ex.printStackTrace();
        }

        MediaCallManager.getInstance().removeStateListener(_stateChangeListener);
    }

    void openStream() {
        try {
            _rtVideo = MMCSDK.CreateRealTimeVideo(this);
            _rtVideo.ConnectCamera((IRealTimeVideo.ICameraConnector)_camera);

            _callView = _rtVideo.CreateVideoPlaybackView();
            FrameLayout callView = (FrameLayout) findViewById(R.id.mv_call_view);
            FrameLayout.LayoutParams fparam = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            callView.addView(_callView, fparam);

            _rtVideo.Start(_hpspUrl, _uid);
            _steamClosed = false;
        }
        catch (MMCException ex) {
            ex.printStackTrace();
        }
    }

    void closeStream(){
        if (_steamClosed){
            return;
        }

        try {
            if (_rtVideo != null) {
                _rtVideo.Stop();
            }
        }
        catch (MMCException ex) {
            ex.printStackTrace();
        }

        _steamClosed = true;
    }

    @Override
    public void HandleRealTimeVideoPeerStatus(IRealTimeVideo.PeerStatus peerStatus, String s) {
        if (peerStatus == IRealTimeVideo.PeerStatus.Alive) {
            try {
                _rtVideo.AssociatePeerWithVideoPlaybackView(_callView, s);
            }
            catch (MMCException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void HandleRealTimeVideoNetworkStatus(IRealTimeVideo.NetworkStatus networkStatus) {

    }
}
