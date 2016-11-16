package main

import (
	"encoding/json"
	"net/http"
	//	"strconv"
	"io/ioutil"
	"strings"
)

/*
     Method         = "OPTIONS"                ; Section 9.2
                    | "GET"                    ; Section 9.3
                    | "HEAD"                   ; Section 9.4
                    | "POST"                   ; Section 9.5
                    | "PUT"                    ; Section 9.6
                    | "DELETE"                 ; Section 9.7
                    | "TRACE"                  ; Section 9.8
                    | "CONNECT"                ; Section 9.9
                    | extension-method
   extension-method = token
     token          = 1*<any CHAR except CTLs or separators>
*/

var _Auth = &Auth{}
var _SessionMgr = &SessionManager{}
var _MediaAppServerClient = &MediaCloudClient{}
var _SessionServ = &SessionServer{}

type Server struct {
}

func (this *Server) start() {
	go _MediaAppServerClient.Start("lianmaibiz.hifun.mobi:5000")
	go _SessionServ.Start("0.0.0.0:9300")

	// _DBMgr.Open("appserver", "bizuser", "Biz123456", "rm-bp1gp1fz524p949gs.mysql.rds.aliyuncs.com:3306")
	_DBMgr.Open("appserver", "root", "1234", "localhost:3307")
	_Cache.init()
	_SessionMgr.init()
	_Auth.init()

	http.HandleFunc("/user/", UserHandler)
	http.HandleFunc("/register", RegisterHandler)
	http.HandleFunc("/mediasession/", MediaSessionHandler)
	http.HandleFunc("/", PageNotFoundHandler)

	http.ListenAndServe("0.0.0.0:9800", nil)
}

const ()

/**
  获取用户
  get user
  /user/{id}

  添加用户
  put user
  /user

  删除用户
  delete user
  /user/{id}

  获取所有的用户
  /user/all

*/
func UserHandler(w http.ResponseWriter, r *http.Request) {

	//	sessionid := r.Header.Get("sessionid")

	//	session := _SessionMgr.GetSession(sessionid)

	//	if session == nil {
	//		Log("session not found")

	//		w.Write(KErrorSessionNotFound.toJson())
	//		return
	//	}

	portal := r.Header.Get("portal")

	if len(portal) <= 0 {
		w.Write(KErrorPortalNotFound.toJson())
		return
	}

	if len(_Cache.GetPortalUid(portal)) <= 0 {
		w.Write(KErrorPortalInvalid.toJson())
		return
	}

	if !AuthRequest(w, r, portal) {
		w.Write(KErrorUnauthorizedToken.toJson())
		return
	}

	pathInfo := strings.Trim(r.URL.Path, "/")

	Log("path info :%s", pathInfo)

	r.ParseForm()
	Log("form : %v", r.Form)

	parts := strings.Split(pathInfo, "/")

	controller := &UserController{}

	if len(parts) == 2 {
		if parts[1] == "all" {
			users := controller.GetAllUsers("", portal)

			userMap := make(map[string]interface{})

			userMap["users"] = users

			buff, _ := json.Marshal(userMap)

			w.Write(buff)
		} else {
			if r.Method == "GET" {
				controller.GetUser(parts[1])

			} else if r.Method == "PUT" {

			} else if r.Method == "DELETE" {

			} else {
				PageNotFoundHandler(w, r)
			}
		}

	} else if len(parts) == 1 {
		PageNotFoundHandler(w, r)
	} else {
		PageNotFoundHandler(w, r)
	}
}

func MediaSessionHandler(w http.ResponseWriter, r *http.Request) {
	pathInfo := strings.Trim(r.URL.Path, "/")

	Log("path info :%s", pathInfo)

	portal := r.Header.Get("portal")

	if len(portal) <= 0 {
		w.Write(KErrorPortalNotFound.toJson())
		return
	}

	if len(_Cache.GetPortalUid(portal)) <= 0 {
		w.Write(KErrorPortalInvalid.toJson())
		return
	}

	if strings.Contains(r.URL.Path, "mediasession/create") {
		message := _MediaAppServerClient.PostCreateSessionMessage()

		resp := _MediaAppServerClient.WaitSessionCreationResp(message)

		jsonStr, _ := json.Marshal(resp._proto)

		w.Write(jsonStr)
	} else {
		PageNotFoundHandler(w, r)
	}
}

func RegisterHandler(w http.ResponseWriter, r *http.Request) {
	pathInfo := strings.Trim(r.URL.Path, "/")

	Log("path info :%s", pathInfo)

	r.ParseForm()
	Log("form : %v", r.Form)

	body, err := ioutil.ReadAll(r.Body)

	if err != nil {
		w.Write(KErrorBodyParseError.toJson())
		return
	}

	Log("recv register body : %s", string(body))

	var user = &LoginUser{}

	err = json.Unmarshal(body, user)

	if err != nil {
		w.Write(KErrorBodyParseError.toJson())
		return
	}

	portal := r.Header.Get("portal")

	Log("register header : %V", r.Header)

	if len(portal) <= 0 {

		LogError("portal not found in the header")

		w.Write(KErrorPortalNotFound.toJson())

		return
	}

	if len(_Cache.GetPortalUid(portal)) <= 0 {
		w.Write(KErrorPortalInvalid.toJson())
		return
	}

	if _DBMgr.UserSotre().FindUser(user.Uid, portal) {
		w.Write(KErrorUserExisted.toJson())

		return
	}

	userBean := &User{}
	userBean.Uid = user.Uid
	userBean.Pwd = user.Pwd

	if !_DBMgr.UserSotre().AddUser(userBean, portal) {
		w.Write(KErrorUserRegister.toJson())
		return
	}

	w.Write(KErrorNone.toJson())
}

func PageNotFoundHandler(w http.ResponseWriter, r *http.Request) {
	w.Write(KErrorPageNotFound.toJson())
}

type UserController struct {
}

func (this *UserController) GetAllUsers(uid string, portal string) []*User {

	users := _DBMgr.UserSotre().GetAllUsers(uid, portal)

	for _, user := range users {
		user.State = KStateOffline
		if _SessionMgr.GetSession(user.Uid, portal) != nil {
			user.State = KStateOnline
		}
	}

	return users
}

func (this *UserController) GetUser(uid string) *User {

	return nil

}

func AuthRequest(w http.ResponseWriter, r *http.Request, portal string) bool {
	token := r.Header.Get("token")

	return _Auth.VerifyToken(token, portal)
}
