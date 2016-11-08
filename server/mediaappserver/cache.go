package main

import (
	"sync"
)

var _Cache = &Cache{}

type Cache struct {
	_portals       map[string]string
	_portalCreated map[string]bool

	_portalLock         sync.Mutex
	_portalCreationLock sync.Mutex
}

func (this *Cache) init() {
	this._portals = make(map[string]string)
	this._portalCreated = make(map[string]bool)
}

func (this *Cache) AddPortal(uri string, uid string) {

	this._portalLock.Lock()
	this._portals[uri] = uid
	this._portalLock.Unlock()
}

func (this *Cache) GetPortalUid(uri string) string {
	this._portalLock.Lock()
	if uid, ok := this._portals[uri]; ok {
		this._portalLock.Unlock()
		return uid
	}

	this._portalLock.Unlock()

	uid := _DBMgr.GetPortalUid(uri)

	this._portalLock.Lock()
	this._portals[uri] = uid
	this._portalLock.Unlock()

	return uid
}

func (this *Cache) OnPortalDBCreated(portalUri string) {
	this._portalCreationLock.Lock()
	this._portalCreated[portalUri] = true
	this._portalCreationLock.Unlock()
}

func (this *Cache) PortalCreated(portalUri string) bool {
	this._portalCreationLock.Lock()
	if _, ok := this._portalCreated[portalUri]; ok {
		this._portalCreationLock.Unlock()
		return true
	}

	this._portalCreationLock.Unlock()

	return false
}
