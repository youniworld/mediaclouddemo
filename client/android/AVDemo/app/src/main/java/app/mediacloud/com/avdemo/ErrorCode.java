package app.mediacloud.com.avdemo;

/**
 * Created by youni on 2016/11/3.
 */

public class ErrorCode {
    public final static ErrorCode KErrorNone = new ErrorCode(0,"");
    public final static ErrorCode KErrorGeneral = new ErrorCode(-1,"general error");
    public final static ErrorCode KErrorUserExisted = new ErrorCode(-100,"user alreay existed");

    public final static ErrorCode KConnectionError = new ErrorCode(-1000,"general connection error");


    private int _errorCode;
    private String _errorDesc;

    public ErrorCode(int errorCode, String desc){
        _errorCode = errorCode;
        _errorDesc = desc;
    }

    public int get_errorCode() {
        return _errorCode;
    }

    public String get_errorDesc() {
        return _errorDesc;
    }
}
