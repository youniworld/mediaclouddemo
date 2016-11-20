package app.mediacloud.com.avdemo;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by youni on 2016/11/4.
 */
class TCPClient implements OnProtocolMessageListener{
    private static String TAG = "TCPClient";
    private ExecutorService _executor = Executors.newSingleThreadExecutor();
    private ExecutorService _connectionExecutor = Executors.newSingleThreadExecutor();
    private Socket _socket;
    private IPEndPoint _host;
    private boolean _isConnected;
    private boolean _isLogined;
    private TCPReader _reader;
    private TCPWriter _writer;
    private BlockingQueue<IMediaProtocol> _messageQueue;
    private Object _connLock = new Object();
    private List<OnConnectionListener> _connectionListeners = Collections.synchronizedList(new LinkedList<OnConnectionListener>());
    private List<OnProtocolMessageListener> _messageListeners = Collections.synchronizedList(new LinkedList<OnProtocolMessageListener>());
    private Interceptor _interceptor = new Interceptor();
    private String _uid;

    private class Interceptor implements OnProtocolMessageListener{
        private List<MessageInterceptor> _interceptors = Collections.synchronizedList(new LinkedList<MessageInterceptor>());
        private ExecutorService _executor = Executors.newSingleThreadExecutor();

        @Override
        public void OnProtocolReceived(final List<IMediaProtocol> messages) {
            synchronized (_interceptors){
                _executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            for(MessageInterceptor interceptor:_interceptors){
                                interceptor.OnProtocolReceived(messages);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        public void addInterceptor(MessageInterceptor interceptor){
            if(_interceptors.contains(interceptor)){
                return;
            }

            _interceptors.add(interceptor);
        }

        public void removeInterceptor(MessageInterceptor interceptor){
            _interceptors.remove(interceptor);
        }
    }

    public void addMessageListener(OnProtocolMessageListener listener){
        if(_messageListeners.contains(listener)){
            return;
        }

        _messageListeners.add(listener);
    }

    public void removeMessageListener(OnProtocolMessageListener listener){
        _messageListeners.remove(listener);
    }

    public void addInterceptor(MessageInterceptor interceptor){
        _interceptor.addInterceptor(interceptor);
    }

    public void removeInterceptor(MessageInterceptor interceptor){
        _interceptor.removeInterceptor(interceptor);
    }

    public void addListener(OnConnectionListener ls){
        if(_connectionListeners.contains(ls)){
            return;
        }

        _connectionListeners.add(ls);
    }

    public void removeListener(OnConnectionListener ls){
        _connectionListeners.remove(ls);
    }

    public void send(IMediaProtocol message){
        if (!_isConnected){
            throw new RuntimeException("illegal connection state");
        }

        _messageQueue.offer(message);
    }

    public void connect(String addr) throws Exception{
        synchronized (_connLock){
            _host = new IPEndPoint(addr);

            _socket = new Socket();

            try {
                _socket.connect(new InetSocketAddress(_host.getIp(),_host.getPort()),10*1000);

                _isConnected = true;
            } catch (IOException e) {
                e.printStackTrace();

                _isConnected = false;
                throw e;
            }

            addMessageListener(_interceptor);

            _messageQueue = new LinkedBlockingQueue<IMediaProtocol>();

            _reader = new TCPReader(_socket,this);
            _writer = new TCPWriter(_socket,_messageQueue);

            _reader.Start();
            _writer.Start();
        }

    }

    public boolean isLogined(){
        return _isLogined;
    }

    public void Disconnect(){
        synchronized (_connLock){
            if(!_isConnected){
                return;
            }

            _isConnected = false;
            _isLogined = false;

            if (_reader != null){
                _reader.Stop();
            }

            if (_writer != null){
                _writer.Stop();
            }

            Close();
        }
    }

    public String Login(String uid, String pwd, String portal) throws  Exception{

        LoginProto login = new LoginProto();

        login.set_portal(portal);
        login.set_pwd(pwd);
        login.set_uid(uid);

        MessageInterceptor waitor = new MessageInterceptor(login.get_packetId());
        addInterceptor(waitor);

        _messageQueue.offer(login);
        try{
            waitor.waitMessage(20*1000);

            if (waitor.getMessages() != null && waitor.getMessages().size() > 0){
                IMediaProtocol msg = waitor.getMessages().get(0);

                if (msg instanceof LoginRespProto){
                    LoginRespProto resp = (LoginRespProto) msg;

                    if (resp.is_success()){
                        _isLogined = true;

                        return resp.get_token();
                    }else {
                        _isLogined = false;
                        Disconnect();

                        throw new Exception("login failed");
                    }
                }else {
                    throw new Exception("no resp login returned");
                }
            }else{
                Log.e(TAG,"We Receive no response from server");
                Disconnect();
                throw new Exception("login failed");
            }
        }catch (Exception e){
            throw e;

        }finally {
            removeInterceptor(waitor);
        }

    }

    public void logout(){
        if (!_isLogined){
            return;
        }

        LogoutProto logoutProto = new LogoutProto();

        _messageQueue.offer(logoutProto);

        Disconnect();
    }

    public boolean waitPong(long mills){
        PingProto ping = new PingProto();


        MessageInterceptor waitor = new MessageInterceptor(ping.get_packetId());
        addInterceptor(waitor);

        _messageQueue.offer(ping);
        try{
            waitor.waitMessage(mills);

            if (waitor.getMessages() != null && waitor.getMessages().size() > 0){
                IMediaProtocol msg = waitor.getMessages().get(0);

                if (msg instanceof PingProto){
                    return true;
                }else {
                    return false;
                }
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;

        }finally {
            removeInterceptor(waitor);
        }
    }

    public void notifyOnConnected(){
        _connectionExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    for(OnConnectionListener listener:_connectionListeners){
                        listener.OnConnected();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void notifyOnDisconnected(){
        Log.i(TAG,"the socket is disconnected");

        _connectionExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    for(OnConnectionListener listener:_connectionListeners){
                        listener.OnDisconnected(ErrorCode.KErrorNone);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void Close(){
        _isConnected = false;
        _isLogined = false;

        try {
            _socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Reconnect(){
        synchronized (_connLock){
            if (_isConnected){
                return;
            }

            _isConnected = false;
            Close();

            try {
                _socket.connect(new InetSocketAddress(_host.getIp(),_host.getPort()),10*1000);

                _isConnected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnProtocolReceived(final List<IMediaProtocol> messages) {
        if(messages == null || messages.size() <= 0){
            return;
        }

        if(_messageListeners.size() <= 0){
            return;
        }

        _executor.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    for(OnProtocolMessageListener listener:_messageListeners){
                        listener.OnProtocolReceived(messages);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    class TCPReader {
        private ExecutorService _executor = Executors.newSingleThreadExecutor();

        private Socket _socket;
        private BlockingQueue<IMediaProtocol> _messageQueue;
        private Thread _thread;
        private boolean _stopped = false;
        private OnProtocolMessageListener _listener;
        private ProtocolParser _parser;

        TCPReader(Socket socket, OnProtocolMessageListener listener){
            _socket = socket;
            _listener = listener;
            _parser = new ProtocolParser();
        }

        public void Start(){
            if(_thread != null && _thread.isAlive()){
                return;
            }

            _stopped = false;
            _thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        byte[] buff = new byte[2048];

                        while(!_stopped){
                            int len = _socket.getInputStream().read(buff);

                            if(len <=0){
                                throw new Exception("EOF of input stream");
                            }

                            byte[] protoBuf = new byte[len];

                            System.arraycopy(buff,0,protoBuf,0,len);

                            if (len > 0){
                                final List<IMediaProtocol> protos = _parser.Unmarsal(protoBuf);

                                if (protos != null &&  protos.size() > 0 && _listener != null){
                                    _executor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                _listener.OnProtocolReceived(protos);
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }else{
                                _stopped = true;
                            }
                        }
                    }catch (Exception e){
                        if(_stopped){
                            return;
                        }

                        e.printStackTrace();
                        _stopped = true;
                        Disconnect();
                        notifyOnDisconnected();
                    }
                }
            });

            _thread.start();
        }

        public void Stop(){
            _stopped = true;
        }
    }

    class TCPWriter {
        private BlockingQueue<IMediaProtocol> _messageQueue;
        private Thread _thread;
        private boolean _stopped = false;
        private ProtocolParser _parser = new ProtocolParser();

        TCPWriter(Socket socket, BlockingQueue<IMediaProtocol> queue){
            _messageQueue = queue;
        }

        public void Start(){
            if (_thread != null && _thread.isAlive()){
                return;
            }

            _stopped = false;

            _thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        while (!_stopped){
                            IMediaProtocol proto = _messageQueue.take();

                            if(proto == null){
                                throw new Exception("the proto is null!");
                            }

                            byte[] buff = _parser.Marsal(proto);

                            _socket.getOutputStream().write(buff);
                        }
                    }catch (Exception e){
                        if(_stopped){
                            return;
                        }

                        e.printStackTrace();
                        _stopped = true;
                        Disconnect();
                        notifyOnDisconnected();
                    }
                }
            });

            _thread.start();
        }

        public void Stop(){
            if (_stopped){
                return;
            }

            _stopped = true;

            // tricky solution to stoping the _messageQueue waiting.
            //_messageQueue.offer(null);
        }
    }
}
