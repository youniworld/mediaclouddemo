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

class StateProto extends  IMediaProtocol{
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

                }
            }else if(message.hasCall()){

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

        BigEdian.PutInt(buff,2,protoBuff.length);

        System.arraycopy(protoBuff,0,buff,6,protoBuff.length);

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
