package app.mediacloud.com.avdemo;

import java.util.List;

/**
 * Created by youni on 2016/11/4.
 */
interface OnProtocolMessageListener{
    public void OnProtocolReceived(List<IMediaProtocol> protos);
}
