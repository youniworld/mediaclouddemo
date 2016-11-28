package app.mediacloud.com.avdemo;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by youni on 2016/11/8.
 */

public class ProtoMessage {
}

abstract class IMediaProtocol{
    protected static AtomicLong PacketId = new AtomicLong(0);
    protected long _packetId = 0;

    IMediaProtocol(){
        _packetId = Inc();
    }

    public long Inc(){
        return PacketId.incrementAndGet();
    }

    public void set_packetId(long _packetId) {
        this._packetId = _packetId;
    }

    public long get_packetId() {
        return _packetId;
    }

    abstract  public void Unmarsal(Mediaappsingnal.MediaAppSignalMessage message);
    abstract  public byte[] Marsal();
}

class LoginProto extends IMediaProtocol{
    private String _uid;
    private String _pwd;
    private String _portal;

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public void set_pwd(String _pwd) {
        this._pwd = _pwd;
    }

    public void set_portal(String _portal) {
        this._portal = _portal;
    }

    @Override
    public void Unmarsal(Mediaappsingnal.MediaAppSignalMessage message) {

    }

    @Override
    public byte[] Marsal() {
        //_packetId = Inc();

        Mediaappsingnal.MediaAppSignalMessage.Builder builder = Mediaappsingnal.MediaAppSignalMessage.newBuilder();
        builder.getSignalBuilder().getLoginBuilder()
                .setUid(this._uid)
                .setPwd(this._pwd)
                .setPortal(this._portal);

        builder.getBaseBuilder().setPacketId(_packetId);
        Mediaappsingnal.MediaAppSignalMessage message =  builder.build();


        return message.toByteArray();
    }
}

class LogoutProto extends  IMediaProtocol{

    @Override
    public void Unmarsal(Mediaappsingnal.MediaAppSignalMessage message) {

    }

    @Override
    public byte[] Marsal() {
        Mediaappsingnal.MediaAppSignalMessage.Builder builder = Mediaappsingnal.MediaAppSignalMessage.newBuilder();
        builder.getSignalBuilder().getLogoutBuilder();

        builder.getBaseBuilder().setPacketId(_packetId);
        Mediaappsingnal.MediaAppSignalMessage message =  builder.build();


        return message.toByteArray();
    }
}

class LoginRespProto extends IMediaProtocol {
    private String _token;
    private String _reason;
    private boolean _success;

    @Override
    public void Unmarsal(Mediaappsingnal.MediaAppSignalMessage message) {
        _packetId = message.getBase().getPacketId();

        Mediaappsingnal.MediaSignalMessage.LoginResp resp =  message.getSignal().getLoginResp();

        if (resp.getCode() != 0){
            _success = false;
            _reason = resp.getFailedReason();
        }else{
            _success = true;
            _token = resp.getToken();
        }
    }

    @Override
    public byte[] Marsal() {

        return new byte[0];
    }

    public String get_reason() {
        return _reason;
    }

    public String get_token() {
        return _token;
    }

    public boolean is_success() {
        return _success;
    }
}

class StateProto extends IMediaProtocol{
    private String _uid;
    private int _state;

    @Override
    public void Unmarsal(Mediaappsingnal.MediaAppSignalMessage message) {
        Mediaappsingnal.MediaSignalMessage.OnlineStateChange resp = message.getSignal().getStateChanged();

        _uid = resp.getUid();
        _state = resp.getState();
    }

    @Override
    public byte[] Marsal() {
        return new byte[0];
    }

    public String get_uid() {
        return _uid;
    }

    public int get_state() {
        return _state;
    }
}

class CallProto extends IMediaProtocol{
    private MediaCallMessage _message;
    private String _from;
    private String _to;

    public void set_from(String _from) {
        this._from = _from;
    }

    public String get_from() {
        return _from;
    }

    public void set_to(String _to) {
        this._to = _to;
    }

    public String get_to() {
        return _to;
    }

    public void set_message(MediaCallMessage _message) {
        this._message = _message;
    }

    public MediaCallMessage get_message() {
        return _message;
    }

    @Override
    public void Unmarsal(Mediaappsingnal.MediaAppSignalMessage message) {
        _message = new MediaCallMessage();

        _from = message.getCall().getBase().getFrom();
        _to = message.getCall().getBase().getTo();

        if (message.getCall().hasCallInitiate()){
            _message.set_callCmd(MediaCallMessage.CallCmd.ECallInitiate);
            _message.set_caller(message.getCall().getCallInitiate().getCaller());
            _message.set_sessionId(message.getCall().getCallInitiate().getMediaSession());
        } else if (message.getCall().hasCallAccept()){
            _message.set_callCmd(MediaCallMessage.CallCmd.ECallAccepted);
            _message.set_sessionId(message.getCall().getCallAccept().getCallid());
            _message.set_callee(message.getCall().getCallAccept().getCallee());
        } else if (message.getCall().hasCallTerminate()){
            _message.set_callCmd(MediaCallMessage.CallCmd.ECallTerminate);
            _message.set_sessionId(message.getCall().getCallTerminate().getCallid());

            int reason = message.getCall().getCallTerminate().getReason();

            if (reason == 0){
                _message.set_reason(MediaCallMessage.CallHangupReason.ECallNormal);
            } else if (reason == 1){
                _message.set_reason(MediaCallMessage.CallHangupReason.ECallBusy);
            }
        }
    }

    @Override
    public byte[] Marsal() {
        Mediaappsingnal.MediaAppSignalMessage message = null;

        Mediaappsingnal.MediaAppSignalMessage.Builder builder = Mediaappsingnal.MediaAppSignalMessage.newBuilder();

        builder.getCallBuilder().getBaseBuilder().setFrom(_from).setTo(_to).setPortal(_message.get_portal());

        if (_message.get_callCmd() == MediaCallMessage.CallCmd.ECallInitiate){
            builder.getCallBuilder().getCallInitiateBuilder().setCaller(_message.get_caller()).setCallid(_message.get_sessionId()).setMediaSession(_message.get_sessionId());

        } else if (_message.get_callCmd() == MediaCallMessage.CallCmd.ECallAccepted){
            builder.getCallBuilder().getCallAcceptBuilder().setCallee(_message.get_callee()).setCallid(_message.get_sessionId());

        } else if (_message.get_callCmd() == MediaCallMessage.CallCmd.ECallTerminate){
            builder.getCallBuilder().getCallTerminateBuilder().setCallid(_message.get_sessionId()).setReason(_message.get_reason().ordinal());

        }

        message = builder.build();

        return message.toByteArray();
    }
}

class PingProto extends IMediaProtocol{

    @Override
    public void Unmarsal(Mediaappsingnal.MediaAppSignalMessage message) {
        if(message.hasSignal()){
            if (message.getSignal().hasPong()){
                _packetId = message.getBase().getPacketId();
            }
        }
    }

    @Override
    public byte[] Marsal() {
        Mediaappsingnal.MediaAppSignalMessage.Builder builder = Mediaappsingnal.MediaAppSignalMessage.newBuilder();

        builder.getBaseBuilder().setPacketId(_packetId);

        builder.getSignalBuilder().getPingBuilder();

        return builder.build().toByteArray();
    }
}

class DisconnectProto extends IMediaProtocol{

    public DisconnectProto(){}

    @Override
    public void Unmarsal(Mediaappsingnal.MediaAppSignalMessage message) {

    }

    @Override
    public byte[] Marsal() {
        return new byte[0];
    }
}

class ProtocolParser{
    private static String TAG = "ProtocolParser";

    private byte[] _left;

    public List<IMediaProtocol> Unmarsal(byte[] buff) throws Exception{
        int leftLen = 0;

        if (_left != null){
            leftLen = _left.length;
        }

        byte[] newBuf = new byte[buff.length + leftLen];

        if(_left != null && leftLen > 0){
            System.arraycopy(_left,0,newBuf,0,leftLen);
        }

        System.arraycopy(buff,0,newBuf,leftLen,buff.length);

        _left = newBuf;

        ArrayList<IMediaProtocol> protocols = new ArrayList<IMediaProtocol>();

        while(true){
            if (_left == null || _left.length < 6){
                return protocols;
            }

            if (_left[0] != (byte) 0xFA || _left[1] != (byte)0xAF){
                Log.d(TAG,"protocol header flag error");
                throw new Exception("wrong header");
            }

            int protoLen = BigEdian.ReadInt(_left,2);

            if (_left.length < protoLen + 6){
                return protocols;
            }

            byte[] protoBuf = new byte[protoLen];

            System.arraycopy(_left,6,protoBuf,0,protoLen);

            Mediaappsingnal.MediaAppSignalMessage message = Mediaappsingnal.MediaAppSignalMessage.parseFrom(protoBuf);

            if(_left.length == 6 + protoLen){
                _left = null;
            }else{
                byte[] tailBuf = new byte[_left.length-(6+protoLen)];

                System.arraycopy(_left,6+protoLen,tailBuf,0,tailBuf.length);

                _left = tailBuf;
            }

            Log.i(TAG,"RECV MESSAGE : " + message.toString());

            if (message.hasSignal()){
                if (message.getSignal().hasLoginResp()){
                    Mediaappsingnal.MediaSignalMessage.LoginResp protoResp = message.getSignal().getLoginResp();

                    LoginRespProto resp = new LoginRespProto();

                    resp.Unmarsal(message);

                    protocols.add(resp);

                }else if(message.getSignal().hasStateChanged()){
                    StateProto resp = new StateProto();

                    resp.Unmarsal(message);

                    protocols.add(resp);

                } else if (message.getSignal().hasPong()){
                    PingProto ping = new PingProto();
                    ping.Unmarsal(message);
                    protocols.add(ping);
                }
            }else if(message.hasCall()){
                CallProto call = new CallProto();

                call.Unmarsal(message);

                protocols.add(call);
            }
        }

    }

    public byte[] Marsal(IMediaProtocol message) throws Exception{
        byte[] protoBuff = message.Marsal();

        Mediaappsingnal.MediaAppSignalMessage protoMessage = Mediaappsingnal.MediaAppSignalMessage.parseFrom(protoBuff);

        Log.i(TAG,"SEND MESSAGE : " + protoMessage.toString());


        byte[] buff = new byte[protoBuff.length + 6];

        buff[0] = (byte)0xFA;
        buff[1] = (byte)0xAF;

        BigEdian.PutInt(buff, 2, protoBuff.length);

        System.arraycopy(protoBuff, 0, buff, 6, protoBuff.length);

        return buff;
    }
}

class BigEdian {
    static int ReadInt(byte[]buff, int index){
        int value = 0;

        value |= (buff[index++] & 0xFF) << 24;
        value |= (buff[index++] & 0xFF) << 16;
        value |= (buff[index++] & 0xFF) << 8;
        value |= buff[index] & 0xFF;
        return value;
    }

    static void PutInt(byte[]buff, int index, int value){
        buff[index+3] = (byte)(value & 0xFF);
        buff[index+2] = (byte)((value >> 8) & 0xFF);
        buff[index+1] = (byte)((value >> 16) & 0xFF);
        buff[index]   = (byte)((value >> 24) & 0xFF);
    }
}
