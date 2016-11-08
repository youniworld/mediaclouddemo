package app.mediacloud.com.avdemo;

/**
 * Created by youni on 2016/11/4.
 */

public interface ICallback {
    public void OnSuccess();
    public void OnFailed(ErrorCode error);
}
