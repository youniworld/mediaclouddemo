package app.mediacloud.com.avdemo;

import android.content.Context;

/**
 * Created by youni on 2016/11/9.
 */

public class MediaCallManager {
    public static enum CallState{
        EConnecting("EConnecting"),
        EAccepted("EAccepted"),
        ERinging("ERinging"),
        EHangup("EHangup"),
        EReject("EReject"),
        EInit("EInit");

        CallState(String stateStr) {
            _stateStr = stateStr;
        }

        @Override
        public String toString() {
            return _stateStr;
        }

        private String _stateStr;
    }

    public static interface OnCallStateChangeListener{
        public void OnCallStateChanged(CallState state);
    }

    private Context _context;
    private TCPClient _client;

}
