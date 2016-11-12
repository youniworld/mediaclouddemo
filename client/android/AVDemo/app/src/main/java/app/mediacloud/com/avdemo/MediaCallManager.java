package app.mediacloud.com.avdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by youni on 2016/11/9.
 */

public class MediaCallManager {
    public static String INCOMING_CALL_ACTION = "com.medialcloud.app:incoming.call";

    private static String TAG = "MediaCallManager";

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

    private static MediaCallManager _instance = new MediaCallManager();

    private Context _context;
    private TCPClient _client;
    private CallState _state;
    private MediaCallSessionBase _activeSession;
    private ExecutorService _stateExecutor = Executors.newSingleThreadExecutor();

    private List<OnCallStateChangeListener> _callStateListeners = Collections.synchronizedList(new LinkedList<OnCallStateChangeListener>());
    private AppModel.OnCallMessageListener _callMessageListener = new AppModel.OnCallMessageListener() {
        @Override
        public void OnCallMessageReceived(MediaCallMessage callMessage) {
            Log.i(TAG,"recv the call message : " + callMessage.toString());

            if (_activeSession != null){
                if (_activeSession.get_sessionId().equals(callMessage.get_sessionId())){
                    if (callMessage.get_callCmd().equals(MediaCallMessage.CallCmd.ECallAccepted)){
                        Log.i(TAG,"recv the cmd ECallAccepted");
                        _activeSession.set_state(CallState.EAccepted);
                    } else if (callMessage.get_callCmd().equals(MediaCallMessage.CallCmd.ECallTerminate)){
                        Log.i(TAG,"recv the cmd ECallTerminate with reason : " + callMessage.get_reason());

                        if (callMessage.get_reason() == MediaCallMessage.CallHangupReason.ECallNormal){
                            _activeSession.set_state(CallState.EHangup);
                        }else if (callMessage.get_reason() == MediaCallMessage.CallHangupReason.ECallBusy){
                            _activeSession.set_state(CallState.EReject);
                        }

                    } else {
                        Log.w(TAG,"ignore the call message cmd : " + callMessage.get_callCmd());
                    }
                }else{
                    if (callMessage.get_callCmd().equals(MediaCallMessage.CallCmd.ECallInitiate)){
                        MediaCallSessionBase session = new MediaReceiveCallSession(callMessage.get_sessionId());

                        session.set_callManager(MediaCallManager.getInstance());
                        session.set_client(_client);
                        session.set_caller(callMessage.get_caller());
                        session.set_callee(AppModel.getInstance().getUid());
                        session.set_sessionId(callMessage.get_sessionId());

                        session.set_from(AppModel.getInstance().getUid());
                        session.set_to(callMessage.get_caller());

                        try {
                            session.hangupCall();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.w(TAG,"ignore the call message cmd : " + callMessage.get_callCmd());
                    }
                }

            }else{
                MediaReceiveCallSession session = new MediaReceiveCallSession(callMessage.get_sessionId());

                if (callMessage.get_callCmd().equals(MediaCallMessage.CallCmd.ECallInitiate)){
                    _activeSession = session;
                    _activeSession.set_callManager(MediaCallManager.getInstance());
                    _activeSession.set_client(_client);
                    _activeSession.set_caller(callMessage.get_caller());
                    _activeSession.set_callee(AppModel.getInstance().getUid());
                    _activeSession.set_sessionId(callMessage.get_sessionId());
                    _activeSession.set_from(AppModel.getInstance().getUid());
                    _activeSession.set_to(callMessage.get_caller());

                    try {
                        _activeSession.onReceiveInCommingCall();
                    } catch (Exception e) {
                        e.printStackTrace();

                        try {
                            _activeSession.hangupCall();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }


                }else{
                    Log.w(TAG,"ignore the call message cmd : " + callMessage.get_callCmd());
                }
            }
        }
    };

    public static MediaCallManager getInstance() {
        return _instance;
    }

    public void init(Context context, TCPClient client){
        _context = context;
        _client = client;

        AppModel.getInstance().addCallMessageListener(_callMessageListener);

    }

    public void addStateListener(OnCallStateChangeListener listener){
        if(_callStateListeners.contains(listener)){
            return;
        }

        _callStateListeners.add(listener);
    }

    public void removeStateListener(OnCallStateChangeListener listener){
        _callStateListeners.remove(listener);
    }

    void noitfyStateChanged(final CallState state){
        _state = state;

        if (_state == CallState.EHangup || _state == CallState.EReject){
//            try {
//                _activeSession.hangupCall();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            _activeSession = null;
            _state = CallState.EInit;
        } else if (_state == CallState.ERinging){
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(_context);

            Intent intent = new Intent();
            intent.setAction(INCOMING_CALL_ACTION);
            intent.putExtra("uid",_activeSession.get_caller());

            lbm.sendBroadcast(intent);
        }

        if (_callStateListeners.size() > 0){
            _stateExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for(OnCallStateChangeListener listener:_callStateListeners){
                        listener.OnCallStateChanged(state);
                    }
                }
            });
        }
    }

    public void makeCall(String to) throws Exception{
        if (_activeSession != null){
            _activeSession.hangupCall();
        }

        String sessionId = AppModel.getInstance().createSession();

        if (sessionId == null){
            throw new Exception("session id was not created before making a call!!");
        }

        _activeSession = new MediaSendCallSession(sessionId);

        _activeSession.set_caller(AppModel.getInstance().getUid());
        _activeSession.set_callee(to);
        _activeSession.set_client(_client);
        _activeSession.set_callManager(this);
        _activeSession.makeCall(to);
    }

    public void answerCall() throws Exception{
        if (_activeSession == null){
            throw new Exception("no active session ongoing");
        }

        _activeSession.answerCall();
    }

    public void hangupCall() throws Exception{
        if (_activeSession == null){
            throw new Exception("no active session ongoing");
        }

        if (_state == CallState.EAccepted){
            _activeSession.hangupCall();
        } else {
            _activeSession.rejectCall();
        }
    }

    public String getMediaSessionId(){
        if (_activeSession == null){
            return null;
        }

        return _activeSession.get_sessionId();
    }

    public String getIncommingCaller(){
        if (_activeSession == null){
            return null;
        }

        return _activeSession.get_caller();
    }

    public String getPeer(){
        if (_activeSession == null){
            return null;
        }

        if (_activeSession instanceof  MediaSendCallSession){
            return _activeSession.get_callee();
        }else{
            return _activeSession.get_caller();
        }
    }
}

abstract class MediaCallSessionBase{
    protected String _sessionId;
    protected String _caller;
    protected String _callee;
    protected TCPClient _client;
    protected MediaCallManager _callManager;
    protected MediaCallManager.CallState _state;
    protected String _from;
    protected String _to;

    protected MediaCallSessionBase(String sessionId){
        _sessionId = sessionId;
    }

    public void set_callee(String _callee) {
        this._callee = _callee;
    }

    public String get_callee() {
        return _callee;
    }

    public void set_caller(String _caller) {
        this._caller = _caller;
    }

    public String get_caller() {
        return _caller;
    }

    public void set_sessionId(String _sessionId) {
        this._sessionId = _sessionId;
    }

    public String get_sessionId() {
        return _sessionId;
    }

    public void set_client(TCPClient _client) {
        this._client = _client;
    }

    public void set_callManager(MediaCallManager _callManager) {
        this._callManager = _callManager;
    }

    public MediaCallManager.CallState get_state() {
        return _state;
    }

    public void set_state(MediaCallManager.CallState state) {
        _state = state;

        _callManager.noitfyStateChanged(_state);
    }

    public void set_from(String _from) {
        this._from = _from;
    }

    public void set_to(String _to) {
        this._to = _to;
    }

    public void hangupCall() throws Exception{
        MediaCallMessage callMessage = new MediaCallMessage();
        callMessage.set_sessionId(_sessionId);
        callMessage.set_caller(_caller);
        callMessage.set_callee(_callee);
        callMessage.set_portal(AppModel.getInstance().getPortal());
        callMessage.set_callCmd(MediaCallMessage.CallCmd.ECallTerminate);
        callMessage.set_reason(MediaCallMessage.CallHangupReason.ECallNormal);

        CallProto proto = new CallProto();
        proto.set_message(callMessage);
        proto.set_from(_from);
        proto.set_to(_to);

        _state = MediaCallManager.CallState.EHangup;

        _callManager.noitfyStateChanged(_state);

        _client.send(proto);
    }

    public void rejectCall() throws Exception{
        MediaCallMessage callMessage = new MediaCallMessage();
        callMessage.set_sessionId(_sessionId);
        callMessage.set_caller(_caller);
        callMessage.set_callee(_callee);
        callMessage.set_portal(AppModel.getInstance().getPortal());
        callMessage.set_callCmd(MediaCallMessage.CallCmd.ECallTerminate);
        callMessage.set_reason(MediaCallMessage.CallHangupReason.ECallBusy);

        CallProto proto = new CallProto();
        proto.set_message(callMessage);
        proto.set_from(_from);
        proto.set_to(_to);

        _state = MediaCallManager.CallState.EHangup;

        _callManager.noitfyStateChanged(_state);

        _client.send(proto);
    }

    abstract void makeCall(String to) throws Exception;
    abstract void answerCall() throws Exception;
    abstract void onReceiveInCommingCall() throws Exception;
}

class MediaReceiveCallSession extends MediaCallSessionBase{

    protected MediaReceiveCallSession(String sessionId) {
        super(sessionId);
    }

    @Override
    void makeCall(String to) throws Exception {
        throw new Exception("this is the receive call session!!, please check the state");
    }

    public void answerCall() throws Exception{
        MediaCallMessage callMessage = new MediaCallMessage();
        callMessage.set_sessionId(_sessionId);
        callMessage.set_caller(_caller);
        callMessage.set_callee(_callee);
        callMessage.set_portal(AppModel.getInstance().getPortal());
        callMessage.set_callCmd(MediaCallMessage.CallCmd.ECallAccepted);

        _from = AppModel.getInstance().getUid();
        _to = _caller;

        CallProto proto = new CallProto();
        proto.set_message(callMessage);
        proto.set_from(_from);
        proto.set_to(_to);

        _client.send(proto);

        _state = MediaCallManager.CallState.EAccepted;

        _callManager.noitfyStateChanged(_state);
    }

    @Override
    void onReceiveInCommingCall() throws Exception {
        set_state(MediaCallManager.CallState.ERinging);
    }
}

class MediaSendCallSession extends MediaCallSessionBase{

    protected MediaSendCallSession(String sessionId) {
        super(sessionId);
    }

    public void makeCall(String to) throws Exception{
        MediaCallMessage callMessage = new MediaCallMessage();
        callMessage.set_sessionId(_sessionId);
        callMessage.set_caller(_caller);
        callMessage.set_callee(_callee);
        callMessage.set_portal(AppModel.getInstance().getPortal());
        callMessage.set_callCmd(MediaCallMessage.CallCmd.ECallInitiate);

        _from = AppModel.getInstance().getUid();
        _to = to;

        CallProto proto = new CallProto();
        proto.set_message(callMessage);
        proto.set_from(AppModel.getInstance().getUid());
        proto.set_to(_to);

        _client.send(proto);

        _state = MediaCallManager.CallState.EConnecting;
        _callManager.noitfyStateChanged(_state);
    }

    @Override
    void answerCall() throws Exception {
        throw new Exception("this is the send call session!!, please check the state");
    }

    @Override
    void onReceiveInCommingCall() throws Exception {
        throw new Exception("this is the send call session!!, please check the state");
    }
}
