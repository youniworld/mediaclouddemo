package main

import (
	"database/sql"
	"encoding/json"
	"fmt"

	_ "github.com/go-sql-driver/mysql"
)

/**
* portal db  -- contian the protal uid
*
* user db    -- the users
* session db -- contain the tcp connection
**/

// portal db
const (
	KPortalDB  = "portal"
	KPortalURI = "uri"
	KPortalUid = "uid"
)

// user db
const (
	KUserToken  = "_token"
	KUserUid    = "_uid"
	KUserPwd    = "_pwd"
	KUserNick   = "_nick"
	KUserAvatar = "_avatar"
)

// session db
const (
	KSessionId      = "_sessionid"
	KSessionUserUid = "_uid"
	KSession        = "_session"
)

var _DBMgr = &DBManager{}
var _AuthedPortalCache = make(map[string]string)

type DBManager struct {
	_dbName     string
	_dbPwd      string
	_dbUser     string
	_datasource string

	SessionDB *DBSession
	UserDB    *DBUser
	TokenDB   *DBToken
	_db       *sql.DB
}

type DBSession struct {
	_db *sql.DB
}

type DBToken struct {
	_db *sql.DB
}

func (this *DBManager) Open(dbname string, dbuser string, dbpwd string, dburl string) {
	this._dbName = dbname
	this._dbUser = dbuser
	this._dbPwd = dbpwd

	this._datasource = fmt.Sprintf("%s:%s@tcp(%s)/%s?charset=utf8", dbuser, dbpwd, dburl, dbname)

	var err error
	this._db, err = sql.Open("mysql", this._datasource)

	if err != nil {
		LogError(err.Error())

		panic(err)
	}

	Log("db is opened : %s", this._datasource)
}

func (this *DBManager) CreatePortal(portal string) bool {
	if _Cache.PortalCreated(portal) {
		return true
	}

	uid := _Cache.GetPortalUid(portal)

	if len(uid) <= 0 {
		return false
	}

	if !CreateSessionTable(this._db, uid) {
		LogError("create session table failed")
		return false
	}

	if !CreateUserTable(this._db, uid) {
		LogError("create user table failed")
		return false
	}

	_Cache.OnPortalDBCreated(portal)

	return true
}

func CreateSessionTable(db *sql.DB, uid string) bool {
	sql_create_table := fmt.Sprintf("create table if not exists %s_session ("+
		"_id int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,"+
		"_uid varchar(200) NOT NULL,"+
		"_sessionid varchar(200) NOT NULL,"+
		"_session varchar(1000) NOT NULL"+
		")", uid)

	//Log("create segment table : %s", sql_create_table)

	return CreateTable(sql_create_table, db)
}

//const (
//	KUserToken  = "_token"
//	KUserUid    = "_uid"
//	KUserPwd    = "_pwd"
//	KUserNick   = "_nick"
//	KUserAvatar = "_avatar"
//)
func CreateUserTable(db *sql.DB, uid string) bool {
	sql_create_table := fmt.Sprintf("create table if not exists %s_user ("+
		"_id int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,"+
		"_uid varchar(200) NOT NULL,"+
		"_pwd varchar(200) NOT NULL,"+
		"_nick varchar(1000) NOT NULL,"+
		"_avatar varchar(1000) NOT NULL,"+
		"_token varchar(1000) NOT NULL,"+
		"UNIQUE (_uid)"+
		")", uid)

	return CreateTable(sql_create_table, db)
}

func CreateTable(sql string, db *sql.DB) bool {
	Log("create sql : %s \r\n", sql)

	stmt, err := db.Prepare(sql)

	if err != nil {
		LogError("CreateTable : %s " + err.Error())

		return false
	}

	if stmt != nil {
		stmt.Exec()
		stmt.Close()
	}

	return true
}

func (this *DBManager) SessionStore() *DBSession {
	if this.SessionDB == nil {
		this.SessionDB = &DBSession{}
		this.SessionDB._db = this._db
	}

	return this.SessionDB
}

func (this *DBManager) UserSotre() *DBUser {
	if this.UserDB == nil {
		this.UserDB = &DBUser{}
		this.UserDB._db = this._db
	}

	return this.UserDB
}

func (this *DBManager) TokenStore() *DBToken {
	if this.TokenDB == nil {
		this.TokenDB = &DBToken{}
		this.TokenDB._db = this._db
	}

	return this.TokenDB
}

func (this *DBManager) GetPortalUid(uri string) string {
	sql := fmt.Sprintf("select %s from %s where %s = '%s'", KPortalUid, KPortalDB, KPortalURI, uri)

	Log("GetPortalUid sql : %s", sql)

	rows, err := this._db.Query(sql)

	if err != nil {

		LogError("GetPortalUid queary error : %s", err.Error())
		return ""
	}

	if rows.Next() {
		var uid string

		rows.Scan(&uid)

		return uid
	}

	return ""
}

func (this *DBSession) SaveSession(session *Session) bool {
	uid := _Cache.GetPortalUid(session.portal)

	sessionJson, _ := json.Marshal(session)

	sql := fmt.Sprintf("insert into %s_session (%s,%s,%s) values('%s','%s','%s')", uid, KSessionId, KSessionUserUid, KSession, session.SessionId, session.uid, string(sessionJson))

	return ExecuteSql(this._db, sql)
}

func (this *DBSession) FindSession(uid string, portal string) *Session {
	portalUid := _Cache.GetPortalUid(portal)

	sql := fmt.Sprintf("select * from %s_session where %s= '%s'", portalUid, KSessionUserUid, uid)

	rows, err := this._db.Query(sql)

	if err != nil {
		LogError(err.Error())

		return nil
	}

	for rows.Next() {
		var id int
		var uid string
		var sessionId string
		var sessionJson string

		err = rows.Scan(&id, &uid, &sessionId, &sessionJson)

		if err != nil {
			LogError(err.Error())
			return nil
		}

		session := &Session{}

		err = json.Unmarshal([]byte(sessionJson), session)

		if err != nil {
			LogError(err.Error())
			return nil
		}
	}

	return nil
}

func (this *DBSession) DeleteSession(uid string, portal string) bool {
	portalUid := _Cache.GetPortalUid(portal)

	sql := fmt.Sprintf("delete from %s_session where %s='%s'", portalUid, KSessionUserUid, uid)

	return ExecuteSql(this._db, sql)
}

type DBUser struct {
	_db *sql.DB
}

//const (
//	KUserToken  = "_token"
//	KUserUid    = "_uid"
//	KUserPwd    = "_pwd"
//	KUserNick   = "_nick"
//	KUserAvatar = "_avatar"
//)

func (this *DBUser) AddUser(user *User, portal string) bool {
	portalUid := _Cache.GetPortalUid(portal)

	CreateUserTable(this._db, portalUid)

	sql := fmt.Sprintf("insert into %s_user (%s,%s,%s,%s,%s) values ('%s','%s','%s','%s','%s')", portalUid, KUserUid, KUserPwd, KUserNick, KUserAvatar, KUserToken, user.Uid, user.Pwd, user.Uid, "", "")

	return ExecuteSql(this._db, sql)
}

func (this *DBUser) DeleteUser(uid string, portal string) bool {
	//portalUid := _Cache.GetPortalUid(portal)

	return false
}

func (this *DBUser) GetAllUsers(uid string, portal string) []*User {
	portalUid := _Cache.GetPortalUid(portal)

	sql := fmt.Sprintf("select * from %s_user", portalUid)

	rows, err := this._db.Query(sql)

	if err != nil {
		LogError(err.Error())
		return nil
	}

	users := make([]*User, 0)
	for rows.Next() {
		var id int
		var uid_ string
		var pwd string
		var nick string
		var avatar string
		var token string

		err = rows.Scan(&id, &uid_, &pwd, &nick, &avatar, &token)

		if err != nil {
			LogError(err.Error())
			continue
		}

		user := &User{}
		user.Nick = nick
		user.Pwd = pwd
		user.Uid = uid_

		users = append(users, user)
	}

	return users
}

func (this *DBUser) UpdateUser(user *User, portal string) bool {

	return false
}

func (this *DBUser) FindUser(uid string, portal string) bool {

	portalUid := _Cache.GetPortalUid(portal)

	CreateUserTable(this._db, portalUid)

	sql := fmt.Sprintf("select * from %s_user where %s = '%s'", portalUid, KUserUid, uid)

	rows, err := this._db.Query(sql)

	if err != nil {
		LogError(err.Error())

		return false
	}

	if rows.Next() {
		return true
	}

	return false
}

func (this *DBUser) AuthUser(uid string, pwd string, portal string) bool {
	portalUid := _Cache.GetPortalUid(portal)

	sql := fmt.Sprintf("select * from %s_user where %s = '%s' and %s = '%s'", portalUid, KUserUid, uid, KUserPwd, pwd)

	rows, err := this._db.Query(sql)

	if err != nil {
		LogError(err.Error())
		return false
	}

	if rows.Next() {
		return true
	}

	return false
}

func (this *DBToken) FindToken(uid string, pwd string, portal string) string {
	portalUid := _Cache.GetPortalUid(portal)

	sql := fmt.Sprintf("select %s from %s_user where %s='%s'", KUserToken, portalUid, KUserUid, uid)

	Log("FindToken >>> sql : %s", sql)

	rows, err := this._db.Query(sql)

	if err != nil {
		LogError("FindToken >>> : %s", err.Error())
		return ""
	}

	if rows.Next() {
		var token string

		err = rows.Scan(&token)

		if err != nil {
			LogError("FindToken >>> : %s", err.Error())
			return ""
		}

		Log("FindToken >>> token : %s", token)
		return token
	}

	return ""
}

func (this *DBToken) TokenExisted(token string, portal string) bool {
	portalUid := _Cache.GetPortalUid(portal)

	sql := fmt.Sprintf("select * from %s_user where %s='%s'", portalUid, KUserToken, token)

	rows, err := this._db.Query(sql)

	if err != nil {
		return false
	}

	if rows.Next() {
		return true
	}

	return false
}

func (this *DBToken) SaveToken(token string, uid string, portal string) bool {
	portalUid := _Cache.GetPortalUid(portal)

	sql := fmt.Sprintf("update %s_user set %s='%s' where %s = '%s'", portalUid, KUserToken, token, KUserUid, uid)

	return ExecuteSql(this._db, sql)
}

func ExecuteSql(db *sql.DB, sql string) bool {
	Log("ExecuteSql>>> : %s  \r\n", sql)

	stmt, err := db.Prepare(sql)

	if err != nil {
		LogError("RECV Prepare error : %s \r\n", err.Error())

		return false
	}

	_, err = stmt.Exec()

	if err != nil {
		LogError("RECV stmt.Exec() error : %s  \r\n", err.Error())

		return false
	}
	return true
}
