package app.mediacloud.com.avdemo;

/**
 * Created by youni on 2016/11/9.
 */
public class MediaCallMessage {
    private CallHangupReason _reason;
    private CallCmd _callCmd;
    private String _caller;
    private String _sessionId;
    private String _callee;
    private String _portal;

    public void set_reason(CallHangupReason _reason) {
        this._reason = _reason;
    }

    public void set_callCmd(CallCmd _callCmd) {
        this._callCmd = _callCmd;
    }

    public void set_caller(String _caller) {
        this._caller = _caller;
    }

    public void set_sessionId(String _sessionId) {
        this._sessionId = _sessionId;
    }

    public void set_callee(String _callee) {
        this._callee = _callee;
    }

    public CallCmd get_callCmd() {
        return _callCmd;
    }

    public String get_portal() {
        return _portal;
    }

    public String get_sessionId() {
        return _sessionId;
    }

    public void set_portal(String _portal) {
        this._portal = _portal;
    }

    public String get_caller() {
        return _caller;
    }

    public String get_callee() {
        return _callee;
    }

    public CallHangupReason get_reason() {
        return _reason;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("session id : %s, caller : %s, callCmd : %s, portal : %s", _sessionId,_caller,_callCmd,_portal));

        return  sb.toString();
    }

    public enum CallHangupReason {ECallBusy, ECallNormal}

    public enum CallCmd {ECallAccepted, ECallTerminate, ECallInitiate}
}
