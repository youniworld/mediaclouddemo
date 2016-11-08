package main

import (
	"crypto/md5"
	"encoding/base64"
	"encoding/binary"
	"sync"
	"time"
)

type Auth struct {
	Tokens      map[string]string
	TokensByUid map[string]string

	_tokenLock      sync.Mutex
	_tokenByUidLock sync.Mutex
}

func (this *Auth) init() {
	if this.Tokens == nil {
		this.Tokens = make(map[string]string)
	}

	if this.TokensByUid == nil {
		this.TokensByUid = make(map[string]string)
	}
}

func (this *Auth) AssignToken(uid string, pwd string, portal string) string {
	token := GenerateToken(uid, pwd, portal)

	this._tokenLock.Lock()
	this.Tokens[token] = SESSIONKEY(uid, portal)
	this._tokenLock.Unlock()

	this._tokenByUidLock.Lock()
	this.TokensByUid[SESSIONKEY(uid, portal)] = token
	this._tokenByUidLock.Unlock()

	go _DBMgr.TokenStore().SaveToken(token, uid, portal)

	return token
}

func (this *Auth) VerifyUser(uid string, pwd string, portal string) bool {
	return _DBMgr.UserSotre().AuthUser(uid, pwd, portal)
}

func (this *Auth) VerifyToken(token string, portal string) bool {
	this._tokenLock.Lock()
	if _, ok := this.Tokens[token]; !ok {
		this._tokenLock.Unlock()
		return _DBMgr.TokenStore().TokenExisted(token, portal)
	}

	this._tokenLock.Unlock()
	return true
}

func (this *Auth) GetTokenByUid(uid string, pwd string, portal string) string {
	this._tokenByUidLock.Lock()
	if _, ok := this.TokensByUid[SESSIONKEY(uid, portal)]; !ok {
		this._tokenByUidLock.Unlock()
		token := _DBMgr.TokenStore().FindToken(uid, pwd, portal)

		if len(token) <= 0 {
			token = this.AssignToken(uid, pwd, portal)
		} else {
			this._tokenLock.Lock()
			this.Tokens[token] = SESSIONKEY(uid, portal)
			this._tokenLock.Unlock()

			this._tokenByUidLock.Lock()
			this.TokensByUid[SESSIONKEY(uid, portal)] = token
			this._tokenByUidLock.Unlock()
		}

		return token
	}

	token := this.TokensByUid[SESSIONKEY(uid, portal)]
	this._tokenByUidLock.Unlock()

	return token
}

func GenerateToken(uid string, pwd string, portal string) string {

	hash := md5.New()

	uuid := uint64(time.Now().Nanosecond())

	buff := make([]byte, 8)

	binary.BigEndian.PutUint64(buff, uuid)

	uuidstr := uid + pwd + portal + base64.StdEncoding.EncodeToString(buff)

	hash.Write([]byte(uuidstr))

	buff = hash.Sum(nil)

	return base64.StdEncoding.EncodeToString(buff)
}
