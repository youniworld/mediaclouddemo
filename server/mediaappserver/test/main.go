package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net"
	"net/http"
	"strings"
	"time"

	//proto "github.com/golang/protobuf/proto"
)

func main() {
	//OnTestRegister("youni", "123456")

	client1 := &TestAppTcpClient{}

	client1._portal = "bj.mediacloud.app"
	client1._uid = "youni"
	client1._pwd = "1234"

	go client1.Start("127.0.0.1:9300")

	client2 := &TestAppTcpClient{}
	client2._portal = "bj.mediacloud.app"
	client2._uid = "youni1"
	client2._pwd = "123456"

	client2.Start("127.0.0.1:9300")

	//OnTestGetAllUsers()
}

func OnTestRegister(uid string, pwd string) {
	user := &LoginUser{}

	user.Pwd = pwd
	user.Uid = uid

	buff, _ := json.Marshal(user)

	// case 1. register
	PostRequest("http://127.0.0.1:9800/register", buff)
}

func OnTestSessionCreate() {
	//case 2. create session
	resp, err := http.Get("http://127.0.0.1:9800/mediasession/create")

	if err != nil {
		fmt.Sprintf("session create error : %s\r\n", err.Error())

		return
	}

	buff, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		fmt.Println(err.Error())

		return
	}

	fmt.Sprintf("session resp : %s \r\n", string(buff))
}

func OnTestGetAllUsers() {
	buff := make([]byte, 1)
	PostRequest("http://127.0.0.1:9800/user/all", buff)
}

func OnHttpTest() {
	OnTestRegister("youni", "1234")

	OnTestSessionCreate()
}

func PostRequest(url string, buff []byte) {
	body := strings.NewReader(string(buff))
	req, err := http.NewRequest("POST", url, body)

	if err != nil {
		fmt.Println(err.Error())

		return
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("portal", "bj.mediacloud.app")
	req.Header.Set("token", "2535dccc3700aa3174b5cef789d236c3")

	client := &http.Client{}

	resp, err := client.Do(req)

	if err != nil {
		fmt.Println(err.Error())
		return
	}

	bdy, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		fmt.Println(err.Error())

		return
	}

	fmt.Println("recv body : " + string(bdy))
}

type TestAppTcpClient struct {
	_uid    string
	_pwd    string
	_portal string
	_parser *ProtoParser
	_conn   net.Conn
}

func (this *TestAppTcpClient) Start(addr string) {
	this._parser = &ProtoParser{}

	conn, err := net.DialTimeout("tcp", addr, time.Duration(10)*time.Second)

	if err != nil {
		LogError(err.Error())

		return
	}

	this._conn = conn

	login := &LoginProto{}

	login._portal = this._portal
	login._user = this._uid
	login._pwd = this._pwd

	buff := this._parser.Marsal(login)

	this._conn.Write(buff)

	buff = make([]byte, 2048)

	for {
		length, err := this._conn.Read(buff)

		if err != nil {
			LogError(err.Error())

			this._conn.Close()

			return
		}

		protos, err := this._parser.UnMarsal(buff[:length])

		if err != nil {
			LogError("unmarsal error : %s", err.Error())

			conn.Close()

			return
		}

		for _, pr := range protos {
			if loginResp, ok := pr.(*LoginProto); ok {
				Log("got token : %s", loginResp._token)

				OnTestGetAllUsers()
			}
		}
	}
}

type LoginUser struct {
	Uid string `json:"uid"`
	Pwd string `json:"pwd"`
}
