package app.mediacloud.com.avdemo;

/**
 * Created by youni on 2016/11/7.
 */
class People {
    private String _nick;
    private String _uid;
    private State _state = State.EOffline;


    public static enum State{
        EOnline,
        EOffline
    }

    public void set_state(State _state) {
        this._state = _state;
    }

    public State get_state() {
        return _state;
    }

    public void set_nick(String _nick) {
        this._nick = _nick;
    }

    public String get_nick() {
        return _nick;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public String get_uid() {
        return _uid;
    }
}
