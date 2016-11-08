package app.mediacloud.com.avdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    public static interface OnUserStateChangedListener{
        public void OnUserStateChanged(String uid, People.State state);
    }

    private Context _context;
    private ExecutorService _loginExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService _connExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService _messageRecvExecutor = Executors.newSingleThreadExecutor();

    private static AppModel instance = new AppModel();
    private static TCPClient _client = new TCPClient();
    private final static String STORE = "_avdemo";
    private String _token;
    private boolean isLoggined = false;
    private String _uid;
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
                }
            }
        }
    };

    private List<OnConnectionListener> _connectionListeners = Collections.synchronizedList(new LinkedList<OnConnectionListener>());
    private List<OnUserStateChangedListener> _userStateChangeListeners = Collections.synchronizedList(new LinkedList<OnUserStateChangedListener>());

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
                notifyConnected();
            }

            @Override
            public void OnDisconnected(final ErrorCode error) {
                notifyDisconnected(error);
            }
        });

        _client.addMessageListener(_messageListener);
    }

    public ErrorCode Register(String uid, String pwd, String portal) {
        HashMap<String,Object> header = new HashMap<String, Object>();

        header.put("uid",uid);
        header.put("pwd",pwd);

        String jsonStr = HttpClient.Post("http://169.254.66.109:9800/register",header);

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

        String jsonStr = HttpClient.Get("http://169.254.66.109:9800/user/all",header);

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

    public void Login(final String uid, final String pwd, final String portal, final ICallback callback){
        _loginExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(_client.isLogined()){
                    callback.OnSuccess();
                    return;
                }else{
                    _client.Disconnect();
                }

                try {
                    _client.connect("169.254.66.109:9300");
                    _token = _client.Login(uid,pwd,portal);
                    isLoggined = true;
                    _uid = uid;
                    callback.OnSuccess();

                    notifyConnected();
                } catch (Exception e) {
                    e.printStackTrace();

                    callback.OnFailed(ErrorCode.KConnectionError);

                    notifyDisconnected(ErrorCode.KConnectionError);
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

        pref.edit().putString("uid","").putString("pwd","").putString("portal","").commit();
    }

    public String get_LoginToken() {
        return _token;
    }

    public String get_uid() {
        return _uid;
    }

    public void saveUser(String uid, String pwd, String portal){
        SharedPreferences pref = _context.getSharedPreferences(STORE,Context.MODE_PRIVATE);

        pref.edit().putString("uid",uid).putString("pwd",pwd).putString("portal",portal).commit();
    }

    public String getUid(){
        SharedPreferences pref = _context.getSharedPreferences(STORE,Context.MODE_PRIVATE);

        return pref.getString("uid","");
    }

    public String getPwd(){
        SharedPreferences pref = _context.getSharedPreferences(STORE,Context.MODE_PRIVATE);

        return pref.getString("pwd","");
    }

    public String getPortal(){
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
