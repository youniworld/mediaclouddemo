package app.mediacloud.com.avdemo;

public interface OnConnectionListener{
    public void OnConnected();
    public void OnDisconnected(ErrorCode error);
}
