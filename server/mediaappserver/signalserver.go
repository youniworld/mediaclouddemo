package main

import (
	"net"
	"sync"
	"time"
)

type SessionServer struct {
	_SessionHandlers map[*Session]*SessionHandler
	_HandlerMapLock  sync.Mutex
}

func (this *SessionServer) Start(bindaddr string) {
	this._SessionHandlers = make(map[*Session]*SessionHandler)

	addr, err := net.ResolveTCPAddr("tcp", bindaddr)

	if err != nil {
		LogError(err.Error())
		return
	}

	listener, err := net.ListenTCP("tcp", addr)

	Log("start to listen the incomming connection")

	if err != nil {
		LogError(err.Error())

		return
	}

	for {
		conn, err := listener.AcceptTCP()

		Log("recv the connection : " + conn.RemoteAddr().String())

		if err != nil {
			continue
		}

		sessionHandler := SessionHandler{}
		sessionHandler._SessionServer = this
		sessionHandler._conn = conn

		// set the keep alive interval
		sessionHandler._conn.SetKeepAlive(true)
		sessionHandler._conn.SetKeepAlivePeriod(time.Duration(300) * time.Second)

		go sessionHandler.Start()
	}
}

type SessionHandler struct {
	_SessionServer *SessionServer
	_session       *Session
	_conn          *net.TCPConn
	_logined       bool
}

func (this *SessionHandler) Start() {
	parser := &ProtoParser{}

	buff := make([]byte, 2048)

	for {
		length, err := this._conn.Read(buff)

		if err != nil {
			LogError(err.Error())

			this.OnLogout()
			return
		}

		protos, err := parser.UnMarsal(buff[:length])

		if err != nil {

			LogError(err.Error())
			this.OnLogout()

			return

		}

		if protos == nil || len(protos) <= 0 {
			continue
		}

		for _, pr := range protos {
			if pr != nil {

				if this._session != nil {
					Log("RECV FROM UID : %s and Portal : %s", this._session.uid, this._session.portal)
				}

				if login, ok := pr.(*LoginProto); ok {
					ok := _SessionMgr.CreatePortal(login._portal)

					if !ok {
						login._success = false
						login._reason = "unauthorized portal"
						this._conn.Write(parser.Marsal(login))
						this._conn.Close()
						return
					}

					yes := _Auth.VerifyUser(login._user, login._pwd, login._portal)

					if !yes {
						login._success = false
						login._reason = "unauthorized user"
						this._conn.Write(parser.Marsal(login))
						this._conn.Close()
						return
					}

					this.OnLogin(login)

					login._token = _Auth.GetTokenByUid(login._user, login._pwd, login._portal)
					_DBMgr.TokenStore().SaveToken(login._token, login._user, login._portal)

					login._success = true
					login._reason = "success"

					this._conn.Write(parser.Marsal(login))

				} else if _, ok := pr.(*LogoutProto); ok {
					this.OnLogout()
				} else if ping, ok := pr.(*PingProto); ok {
					this._conn.Write(parser.Marsal(ping))
				} else if call, ok := pr.(*CallProto); ok {
					to := call._proto.GetXCall().GetXBase().GetXTo()
					portal := call._proto.GetXCall().GetXBase().GetXPortal()

					session := _SessionMgr.GetSession(to, portal)

					this._SessionServer._HandlerMapLock.Lock()
					if hander, ok := this._SessionServer._SessionHandlers[session]; ok {

						buff = parser.Marsal(call)

						hander.Write(buff)
					} else {
						LogError("SessionHandler : call target session is not found")
					}

					this._SessionServer._HandlerMapLock.Unlock()
				}

			}
		}

	}
}

func (this *SessionHandler) Write(buff []byte) {
	if this._conn != nil {
		_, err := this._conn.Write(buff)

		if err != nil {
			LogError(err.Error())
		}
	}
}

func (this *SessionHandler) OnLogin(login *LoginProto) {
	session := _SessionMgr.NewSession(login._user, login._pwd, login._portal)

	this._session = session

	this._SessionServer._HandlerMapLock.Lock()
	this._SessionServer._SessionHandlers[session] = this
	this._SessionServer._HandlerMapLock.Unlock()

	_SessionMgr.SaveSession(session)

	_SessionMgr.PublishSessionChange(uint8(KStateOnline), this._session.uid)

	_SessionMgr.SubscribeSessionChange(this)

	this._logined = true

}

func (this *SessionHandler) OnLogout() {
	this._conn.Close()
	//this._conn = nil

	if !this._logined {
		return
	}

	_SessionMgr.UnSubscribeSessionChange(this)
	if _SessionMgr.DeleteSession(this._session.uid, this._session.portal, this._session) {
		_SessionMgr.PublishSessionChange(uint8(KStateOffline), this._session.uid)
	}

	this._SessionServer._HandlerMapLock.Lock()
	delete(this._SessionServer._SessionHandlers, this._session)
	this._SessionServer._HandlerMapLock.Unlock()

	this._logined = false
}

func (this *SessionHandler) SessionChanged(uid string, state uint8) {
	if this._conn == nil || !this._logined {
		return
	}

	parser := &ProtoParser{}

	if uid != this._session.uid {

		stateChange := &StateProto{}
		stateChange._state = state
		stateChange._user = uid

		this._conn.Write(parser.Marsal(stateChange))
	}

}
