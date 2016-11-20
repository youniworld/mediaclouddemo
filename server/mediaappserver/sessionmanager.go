package main

import (
	"strings"
	"sync"
)

const (
	KStateOffline = iota
	KStateOnline
)

func SESSIONKEY(uid string, portal string) string {
	return portal + ":" + uid
}

func SESSIONKEY_TO_UID_PORTAL(key string) (portal string, uid string) {
	keys := strings.Split(key, ":")

	return keys[0], keys[1]
}

type SessionManager struct {
	Sessions            map[string]*Session
	_SessionSubscribers map[OnlineSessionChanged]OnlineSessionChanged
	_sessionLock        sync.Mutex
	_subscriberLock     sync.Mutex
}

type OnlineSessionChanged interface {
	SessionChanged(uid string, state uint8)
}

func (this *SessionManager) init() {
	this.Sessions = make(map[string]*Session)
	this._SessionSubscribers = make(map[OnlineSessionChanged]OnlineSessionChanged, 0)
}

func (this *SessionManager) NewSession(uid string, pwd string, portal string) *Session {

	session := &Session{}
	session.SessionId = GenerateSessionId(uid, pwd, portal)

	session.uid = uid
	session.portal = portal

	return session

}

func (this *SessionManager) GetSession(uid string, portal string) *Session {
	this._sessionLock.Lock()
	if session, ok := this.Sessions[SESSIONKEY(uid, portal)]; ok {
		this._sessionLock.Unlock()
		return session
	}
	this._sessionLock.Unlock()

	session := _DBMgr.SessionStore().FindSession(uid, portal)

	if session == nil {
		return nil
	}

	this._sessionLock.Lock()
	this.Sessions[uid] = session
	this._sessionLock.Unlock()

	return session
}

func (this *SessionManager) DeleteSession(uid string, portal string, session *Session) bool {
	this._sessionLock.Lock()

	deleted := false

	for _, ses := range this.Sessions {
		if ses == session {
			delete(this.Sessions, SESSIONKEY(uid, portal))
			deleted = true
			break
		}
	}

	this._sessionLock.Unlock()

	if deleted {
		_DBMgr.SessionStore().DeleteSession(uid, portal)
	}

	return deleted
}

func (this *SessionManager) SaveSession(session *Session) {

	this._sessionLock.Lock()
	this.Sessions[SESSIONKEY(session.uid, session.portal)] = session
	this._sessionLock.Unlock()

	_DBMgr.SessionStore().SaveSession(session)
}

func (this *SessionManager) SubscribeSessionChange(subscriber OnlineSessionChanged) {
	this._subscriberLock.Lock()

	this._SessionSubscribers[subscriber] = subscriber

	this._subscriberLock.Unlock()

}

func (this *SessionManager) UnSubscribeSessionChange(subscriber OnlineSessionChanged) {
	this._subscriberLock.Lock()
	delete(this._SessionSubscribers, subscriber)
	this._subscriberLock.Unlock()
}

func (this *SessionManager) PublishSessionChange(state uint8, uid string) {
	this._subscriberLock.Lock()
	for _, v := range this._SessionSubscribers {
		v.SessionChanged(uid, state)
	}
	this._subscriberLock.Unlock()
}

func (this *SessionManager) CreatePortal(portal string) bool {
	if _Cache.PortalCreated(portal) {
		return true
	}

	uid := _Cache.GetPortalUid(portal)

	if len(uid) <= 0 {
		return false
	}

	return _DBMgr.CreatePortal(portal, uid)
}

func GenerateSessionId(uid string, pwd string, portal string) string {
	return GenerateToken(uid, pwd, portal)
}
