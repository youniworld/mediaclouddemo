package app.mediacloud.com.avdemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by youni on 2016/11/4.
 */
class MessageInterceptor implements OnProtocolMessageListener{
    private Object _messageWaitor = new Object();
    private long _id;
    private List<IMediaProtocol> _messages;

    public List<IMediaProtocol> getMessages(){
        return _messages;
    }

    MessageInterceptor(long id){
        _id = id;
    }

    public void waitMessage(long milisecond){
        synchronized (_messageWaitor){
            try {
                _messageWaitor.wait(milisecond);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnProtocolReceived(List<IMediaProtocol> protos) {
        synchronized (_messageWaitor) {
            for (IMediaProtocol protocol : protos) {
                if (protocol.get_packetId() == _id) {
                    if (_messages == null) {
                        _messages = new ArrayList<IMediaProtocol>();
                    }

                    _messages.add(protocol);
                }
            }

            if (_messages != null && _messages.size() > 0) {
                _messageWaitor.notify();
            }
        }
    }
}
