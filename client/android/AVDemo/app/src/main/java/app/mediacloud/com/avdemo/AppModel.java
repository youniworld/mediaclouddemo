package app.mediacloud.com.avdemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by youni on 2016/11/3.
 */

public class AppModel {
    private static String TAG = "AppModel";
    public static interface OnUserStateChangedListener{
        public void OnUserStateChanged(String uid, People.State state);
    }

    public static interface  OnCallMessageListener{
        public void OnCallMessageReceived(MediaCallMessage callMessage);
    }

    private Context _context;
    private ExecutorService _loginExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService _connExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService _messageRecvExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService _pingExecutor = Executors.newSingleThreadExecutor();

    private static AppModel instance = new AppModel();
    private static TCPClient _client = new TCPClient();
    private final static String STORE = "_avdemo";
    private String _token;
    private String _uid;
    private String _pwd;
    private String _portal;
    private boolean _isLogout = false;
    private boolean _autoLogin = false;

    private List<People> _users;
    private OnProtocolMessageListener _messageListener = new OnProtocolMessageListener() {
        @Override
        public void OnProtocolReceived(List<IMediaProtocol> messages) {
            if (messages == null || messages.size() <=0){
                return;
            }

            for (IMediaProtocol message : messages){
                if (message instanceof StateProto){
                   final StateProto sp = (StateProto) message;

                    _messageRecvExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            for (OnUserStateChangedListener listener:_userStateChangeListeners){
                                if (sp.get_state() <= 0){
                                    listener.OnUserStateChanged(sp.get_uid(),People.State.EOffline);
                                }else if (sp.get_state() > 0){
                                    listener.OnUserStateChanged(sp.get_uid(),People.State.EOnline);
                                }
                            }
                        }
                    });
                } else if (message instanceof CallProto){
                    final MediaCallMessage mcm = ((CallProto) message).get_message();

                    _messageRecvExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            for(OnCallMessageListener listener:_callMessageListeners){
                                listener.OnCallMessageReceived(mcm);
                            }
                        }
                    });
                }
            }
        }
    };

    private List<OnConnectionListener> _connectionListeners = Collections.synchronizedList(new LinkedList<OnConnectionListener>());
    private List<OnUserStateChangedListener> _userStateChangeListeners = Collections.synchronizedList(new LinkedList<OnUserStateChangedListener>());
    private List<OnCallMessageListener> _callMessageListeners = Collections.synchronizedList(new LinkedList<OnCallMessageListener>());

    public static AppModel getInstance(){
        return instance;
    }

    void notifyConnected(){
        _connExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    synchronized (_connectionListeners){
                        for(OnConnectionListener listener:_connectionListeners){
                            listener.OnConnected();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    void notifyDisconnected(final ErrorCode error){
        _connExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    synchronized (_connectionListeners){
                        for(OnConnectionListener listener:_connectionListeners){
                            listener.OnDisconnected(error);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    void Init(Context context){
        this._context = context;

        _client.addListener(new OnConnectionListener() {
            @Override
            public void OnConnected() {
                Log.i(TAG,"the connection was connected");
                notifyConnected();
            }

            @Override
            public void OnDisconnected(final ErrorCode error) {
                Log.i(TAG,"connection was disconnected : " + error.get_errorDesc());

                notifyDisconnected(error);

                Reconnect();
            }
        });

        Utils.getInstance().init(_context);

        _client.addMessageListener(_messageListener);

        MediaCallManager.getInstance().init(context, _client);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);

        lbm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent incomingCall = new Intent(_context, ActivityIncomingCall.class);
                incomingCall.putExtra("uid", intent.getStringExtra("uid"));

                incomingCall.setAction(Intent.ACTION_MAIN);
                incomingCall.addCategory(Intent.CATEGORY_LAUNCHER);
                incomingCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(incomingCall);
            }
        }, new IntentFilter(MediaCallManager.INCOMING_CALL_ACTION));

        String pwd = AppModel.getInstance().getPwd();

        // try to auto login
        if(!TextUtils.isEmpty(pwd)) {
            String uid = AppModel.getInstance().getUid();
            String portal = AppModel.getInstance().getPortal();
            AppModel.getInstance().AutoLogin(uid, pwd, portal);
        }
    }

    public ErrorCode Register(String uid, String pwd, String portal) {
        HashMap<String,Object> body = new HashMap<String, Object>();

        body.put("uid",uid);
        body.put("pwd", pwd);

        String jsonStr = HttpClient.Post("http://lianmaibiz.hifun.mobi:9800/register", body, portal);

        if (jsonStr == null){
            return ErrorCode.KErrorGeneral;
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            int errcode = json.getInt("errorcode");

            if (errcode == 0){
                return ErrorCode.KErrorNone;
            }else if (errcode == 3000){
                return ErrorCode.KErrorUserExisted;
            }else {
                return ErrorCode.KErrorGeneral;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ErrorCode.KErrorGeneral;
    }

    public List<People> getAllUsers() {
        return _users;
    }

    public List<People> getAllUsersFromServer(){
        List<People> users = new ArrayList<People>();

        Map<String,String> header = new HashMap<String, String>();
        header.put("token",get_LoginToken());

        String jsonStr = HttpClient.Get("http://lianmaibiz.hifun.mobi:9800/user/all",header,getPortal());

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);

            JSONArray jsonArray = jsonObject.getJSONArray("users");

            if (jsonArray != null){
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject obj = jsonArray.getJSONObject(i);

                    if (obj.getString("uid") != null){
                        People people = new People();

                        people.set_uid(obj.getString("uid"));

                        if(obj.getString("nick") != null){
                            people.set_nick(obj.getString("nick"));
                        }

                        if (obj.has("state")){
                            if (obj.getInt("state") == 0){
                                people.set_state(People.State.EOffline);
                            }else{
                                people.set_state(People.State.EOnline);
                            }
                        }

                        users.add(people);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(users.size() > 0){
            _users = new ArrayList<People>();
            _users.addAll(users);
        }
        return users;

    }

    public String createSession(){
        Map<String,String> header = new HashMap<String, String>();
        header.put("token",get_LoginToken());

        String jsonStr = HttpClient.Get("http://lianmaibiz.hifun.mobi:9800/mediasession/create",header,getPortal());

        if (jsonStr != null){
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                int err = jsonObj.getInt("errcode");

                if (err != 0){
                    return null;
                }

                String sessionid = jsonObj.getString("sessionid");

                if (sessionid != null){
                    return sessionid;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void Login(final String uid, final String pwd, final String portal, final ICallback callback){
        _loginExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(_client.isLogined()){
                    if(callback != null){
                        callback.OnSuccess();
                    }

                    return;
                }else{
                    _client.Disconnect();
                }

                try {
                    SyncLogin(uid,pwd,portal);
                    notifyConnected();

                    if (callback != null){
                        callback.OnSuccess();
                    }

                    startService();
                } catch (Exception e) {
                    e.printStackTrace();

                    callback.OnFailed(ErrorCode.KConnectionError);

                    notifyDisconnected(ErrorCode.KConnectionError);
                }
            }
        });
    }

    public void SyncLogin(final String uid, final String pwd, final String portal) throws Exception{
        _isLogout = false;

        try {
            if (_client.isLogined()){
                return;
            }

            _client.connect("lianmaibiz.hifun.mobi:9300");
            _token = _client.Login(uid,pwd,portal);

            _uid = uid;
            _pwd = pwd;
            _portal = portal;

            _autoLogin = true;

            startPing();
        } catch (Exception e) {
            e.printStackTrace();

            throw e;
        }
    }

    public void AutoLogin(final String uid, final String pwd, final String portal){
        _autoLogin = true;

        _uid = uid;
        _pwd = pwd;
        _portal = portal;

        startService();

        _loginExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SyncLogin(uid, pwd, portal);

                    notifyConnected();
                } catch (Exception e) {
                    Reconnect();
                }
            }
        });
    }

    public void Logout(){
        SharedPreferences pref = _context.getSharedPreferences(STORE,Context.MODE_PRIVATE);

        _client.logout();

        if (_users != null){
            _users.clear();
        }

        _uid = null;

        _isLogout = true;
        _autoLogin = false;

        _portal = null;

        pref.edit().putString("uid","").putString("pwd","").putString("portal","").commit();

        stopService();
    }

    public boolean isLoggined() {
        return _client.isLogined();
    }

    public String get_LoginToken() {
        return _token;
    }

    public void saveUser(String uid, String pwd, String portal){
        SharedPreferences pref = _context.getSharedPreferences(STORE,Context.MODE_PRIVATE);

        pref.edit().putString("uid",uid).putString("pwd",pwd).putString("portal",portal).commit();
    }

    public String getUid(){
        if (_uid != null){
            return _uid;
        }
        SharedPreferences pref = _context.getSharedPreferences(STORE,Context.MODE_PRIVATE);

        _uid = pref.getString("uid","");

        return _uid;
    }

    public String getPwd(){
        SharedPreferences pref = _context.getSharedPreferences(STORE,Context.MODE_PRIVATE);

        return pref.getString("pwd","");
    }

    public String getPortal(){
        if (!TextUtils.isEmpty(_portal)){
            return _portal;
        }

        SharedPreferences pref = _context.getSharedPreferences(STORE,Context.MODE_PRIVATE);

        return pref.getString("portal","");
    }

    public void saveToken(String token, String uid){
        SharedPreferences pref = _context.getSharedPreferences(USER_PRIVATE_STORE(uid),Context.MODE_PRIVATE);

        pref.edit().putString("token",token).commit();

    }

    public String getLocalToken(String uid){
        SharedPreferences pref = _context.getSharedPreferences(USER_PRIVATE_STORE(uid),Context.MODE_PRIVATE);

        return pref.getString("token","");
    }

    public void addConnectionListener(OnConnectionListener ls){
        if(_connectionListeners.contains(ls)){
            return;
        }

        _connectionListeners.add(ls);

        if (_client.isLogined()){
            _connExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        synchronized (_connectionListeners){
                            for(OnConnectionListener listener:_connectionListeners){
                                listener.OnConnected();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

        }else{

        }

    }

    public void removeConnectionListener(OnConnectionListener ls){
        _connectionListeners.remove(ls);
    }

    public void addStateChangeListener(OnUserStateChangedListener listener){
        if (_userStateChangeListeners.contains(listener)){
            return;
        }

        _userStateChangeListeners.add(listener);
    }

    public void removeStateChangeListener(OnUserStateChangedListener listener){
        _userStateChangeListeners.remove(listener);
    }

    public void addCallMessageListener(OnCallMessageListener listener){
        if (_callMessageListeners.contains(listener)){
            return;
        }

        _callMessageListeners.add(listener);
    }

    public void removeCallMessageListener(OnCallMessageListener listener){
        _callMessageListeners.remove(listener);
    }

    private void Reconnect(){
        if (_isLogout){
            return;
        }

        if (!_autoLogin){
            return;
        }

        Log.w(TAG,"start to reconnect...");

        _loginExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);

                    if (_isLogout) {
                        return;
                    }

                    if (!Utils.getInstance().hasConnection()) {
                        Reconnect();
                        return;
                    }

                    SyncLogin(_uid, _pwd, _portal);

                    notifyConnected();
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        return;
                    }

                    Reconnect();
                }
            }
        });
    }

    private void startPing(){
        if (_pendingIntent == null){
            String action = _context.getPackageName() + ":ping";
            Intent pingIntent = new Intent(action);
            _pendingIntent = PendingIntent.getBroadcast(_context,0,pingIntent,0);
            _context.registerReceiver(_pingReceiver,new IntentFilter(action));
        }

        AlarmManager alarmManager = (AlarmManager)_context.getSystemService(Context.ALARM_SERVICE);

        long wakupTime = System.currentTimeMillis() + 3*60*1000;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakupTime,_pendingIntent);
        }else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, wakupTime, _pendingIntent);
        }
    }

    private void stopPing(){

    }

    private void startService(){
        if (_context != null){
            _context.startService(new Intent(_context,StartService.class));
        }
    }

    private void stopService(){
        if (_context != null){
            _context.stopService(new Intent(_context,StartService.class));
        }
    }

    private PingAlarmReceiver _pingReceiver = new PingAlarmReceiver();
    private PendingIntent _pendingIntent;

    private class PingAlarmReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(_isLogout){
                return;
            }

            if (!_client.isLogined()){
                return;
            }

            startPing();

            _pingExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        int cnt = 0;
                        boolean ret = false;
                        do{
                            ret = _client.waitPong(20*1000);

                            if(ret){
                                Log.i("PING","ping successfully");
                                break;
                            }
                        }while (cnt++ < 3);

                        if (!ret){
                            Log.w("PING","ping failed for 3 times and start to reconnect!");

                            _client.Disconnect();
                            Reconnect();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private static String USER_PRIVATE_STORE(String uid){
        return uid + STORE;
    }
}


class IPEndPoint{
    private String ip;
    private int port;

    public IPEndPoint(String addr){
        String ipAndPort[] = addr.split(":");

        ip = ipAndPort[0];
        port = Integer.valueOf(ipAndPort[1]);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
