package main

import (
	"encoding/binary"
	"encoding/json"
	"errors"
	"fmt"
	"net"
	"strconv"
	"sync"
	"time"
)

const (
	KAppToken      = "26742c9e11ab431a8748f6b6e6c90508"
	KAppCredential = "26742c9e11ab431a8748f6b6e6c90508"
)

type PacketID uint64

var GPktId PacketID

var PktLock sync.Mutex

func PakcetId() PacketID {

	PktLock.Lock()
	GPktId++
	PktLock.Unlock()

	return GPktId
}

type MessageResponseListener interface {
	OnMessageRecived([]Message)
}

type Message interface {
	Cmd() string
	RespChan() chan Message
	PacketId() PacketID
}

type CmdCreateSession struct {
	MessageBase
	_proto *MediaAppCreateSessionProto
}

func (this *CmdCreateSession) Cmd() string {
	return "CmdCreateSession"
}

func (this *CmdCreateSession) RespChan() chan Message {
	return this._MessageRespCh
}

func (this *CmdCreateSession) PacketId() PacketID {
	return this._id
}

type MessageBase struct {
	_cmd           string
	_MessageRespCh chan Message
	_id            PacketID
}

type MediaCloudClient struct {
	_messageChan      chan Message
	_messageRecvChan  chan Message
	_addr             string
	_connected        bool
	_conn             net.Conn
	_bytesLeft        []byte
	_messageMap       map[PacketID]Message
	_messageMapLock   sync.Mutex
	_listenerLock     sync.Mutex
	_pingChan         chan bool
	_heartbeatStarted bool
}

func (this *MediaCloudClient) Start(addr string) {
	this._messageChan = make(chan Message, 0)
	this._messageRecvChan = make(chan Message, 0)
	this._messageMap = make(map[PacketID]Message)
	go this.MessageLoop()
	go this.MessageRecvLoop()

	this.connect(addr)
}

func (this *MediaCloudClient) connect(addr string) {
	this._addr = addr

	var conn net.Conn
	var err error

	Log("try to connect to media cloud app server : " + addr)

	for {
		conn, err = net.DialTimeout("tcp", addr, time.Duration(20)*time.Second)

		if err == nil {
			break
		}

		time.Sleep(time.Duration(10) * time.Second)
	}

	Log("the media server was connecgted ... ")

	this._conn = conn

	login := &MediaAppLoginProto{}
	login.Apptoken = KAppToken
	login.Credential = KAppCredential

	buff := this.Marsal(login)

	if buff == nil {
		LogError("the login marsal failed")
		this._conn.Close()

		go this.connect(this._addr)
		return
	}

	_, err = this._conn.Write(buff)

	if err != nil {
		LogError(err.Error())

		go this.connect(this._addr)
		return
	}

	buff = make([]byte, 2048)

	for {
		length, err := this._conn.Read(buff)

		if err != nil {
			LogError(err.Error())

			go this.connect(this._addr)

			return
		}

		protos, err := this.Unmarsal(buff[:length])

		if err != nil {
			LogError(err.Error())

			go this.connect(this._addr)

			return
		}

		for _, pr := range protos {
			if _, ok := pr.(*MediaAppLoginProto); ok {
				this._connected = true

				Log("MediaCloudClient : login successfully")

				this.StopHeartbeat()
				this.StartHeartbeat()

				// for test
				if _Env._test {
					cresteSession := this.PostCreateSessionMessage()

					go func() {
						resp := this.WaitSessionCreationResp(cresteSession)

						Log("resp : %V", resp._proto)
					}()
				}

			} else if resp, ok := pr.(*MediaAppCreateSessionProto); ok {

				buff, err := json.Marshal(resp)

				Log("we got the creation resp : %s", string(buff))

				respMessage := &CmdCreateSession{}
				respMessage._proto = resp
				id, err := strconv.Atoi(resp.CmdId)

				if err != nil {
					LogError(err.Error())

					this._conn.Close()

					go this.connect(this._addr)

					return
				}

				respMessage._id = PacketID(id)

				this._messageRecvChan <- respMessage
			}
		}
	}

}

func (this *MediaCloudClient) StartHeartbeat() {
	go this.StartHeatbeatLoop()
}

func (this *MediaCloudClient) StartHeatbeatLoop() {
	this._heartbeatStarted = true

	var ticker *time.Ticker

	ticker = time.NewTicker(time.Duration(5) * time.Second)

	ping := &MediaAppPingProto{}

	buff := this.Marsal(ping)

	for {
		select {
		case r := <-this._pingChan:
			if !r {
				this._heartbeatStarted = false
				ticker.Stop()

				return
			}

		case <-ticker.C:
			this._conn.Write(buff)
		}
	}
}

func (this *MediaCloudClient) StopHeartbeat() {
	if this._heartbeatStarted {
		this._pingChan <- false
	}
}

func (this *MediaCloudClient) PostCreateSessionMessage() *CmdCreateSession {
	createSession := &CmdCreateSession{}
	createSession._id = PakcetId()
	createSession._MessageRespCh = make(chan Message, 1)

	this.PostMessage(createSession)

	return createSession
}

func (this *MediaCloudClient) PostMessage(message Message) {
	this._messageChan <- message

}

func (this *MediaCloudClient) WaitSessionCreationResp(msg *CmdCreateSession) (resp *CmdCreateSession) {
	respMsg := this.Wait(msg)

	resp, _ = respMsg.(*CmdCreateSession)

	return
}

func (this *MediaCloudClient) Wait(message Message) Message {
	resp := <-message.RespChan()

	return resp
}

func (this *MediaCloudClient) MessageLoop() {
	for {
		message := <-this._messageChan

		if createSession, ok := message.(*CmdCreateSession); ok {
			sessionCreate := &MediaAppCreateSessionProto{}
			sessionCreate.CmdId = fmt.Sprintf("%d", createSession._id)

			config := make(map[string]interface{})

			jsonStr := "{\"players\":[{\"uid\":\"dy_iPad4,4\",\"screenpos\":\"0.1;0.5;0.5;0.5\"}],\"host\":{\"uid\":\"dy_iPhone7,1\"},\"cdn\":{\"url\":\"rtmp://101.201.146.134/hulu/lianmaitest\"}}"
			jsonbuff := []byte(jsonStr)

			json.Unmarshal(jsonbuff, &config)

			sessionCreate.Config = config

			buff := this.Marsal(sessionCreate)

			this._conn.Write(buff)

			Log("send the create session : %V", sessionCreate)

			this._messageMapLock.Lock()
			this._messageMap[createSession._id] = createSession
			this._messageMapLock.Unlock()
		}
	}

}

func (this *MediaCloudClient) MessageRecvLoop() {
	for {
		message := <-this._messageRecvChan

		if createSessionResp, ok := message.(*CmdCreateSession); ok {

			if _, ok := this._messageMap[createSessionResp._id]; ok {
				createSession, _ := this._messageMap[createSessionResp._id].(*CmdCreateSession)

				createSession._MessageRespCh <- createSessionResp

				// after notify the message to the waiter, we should remove the createsession from the session map

				this._messageMapLock.Lock()
				delete(this._messageMap, createSessionResp._id)
				this._messageMapLock.Unlock()

			}
			//this.NotifyListener(createSessionResp)
		}
	}
}

func (this *MediaCloudClient) CreateSession() {

}

type IMediaCloudAppProto interface {
	Marsal() []byte
	UnMarsal(buff []byte)
}

type MediaAppLoginProto struct {
	Cmd        string `json:"cmd"`
	Credential string `json:"credential"`
	Apptoken   string `json:"apptoken"`
	Errcode    int    `json:"errcode"`
}

func (this *MediaAppLoginProto) Marsal() []byte {
	this.Cmd = "login"

	buff, err := json.Marshal(this)

	if err != nil {
		LogError(err.Error())

		return nil
	}

	return buff

}

func (this *MediaAppLoginProto) UnMarsal(buff []byte) {
	json.Unmarshal(buff, this)
}

type MediaAppPingProto struct {
	Cmd string `json:"cmd"`
}

func (this *MediaAppPingProto) Marsal() []byte {
	this.Cmd = "ping"

	buff, err := json.Marshal(this)

	if err != nil {
		LogError(err.Error())

		return nil
	}

	return buff

}

func (this *MediaAppPingProto) UnMarsal(buff []byte) {
	json.Unmarshal(buff, this)
}

type MediaAppCreateSessionProto struct {
	Cmd       string      `json:"cmd"`
	SessionId string      `json:"sessionid"`
	CmdId     string      `json:"cmdid"`
	Errcode   int         `json:"errcode"`
	Config    interface{} `json:"config"`
}

func (this *MediaAppCreateSessionProto) Marsal() []byte {
	this.Cmd = "create-session"

	buff, err := json.Marshal(this)

	if err != nil {
		LogError(err.Error())

		return nil
	}

	return buff
}

func (this *MediaAppCreateSessionProto) UnMarsal(buff []byte) {
	json.Unmarshal(buff, this)
}

func (this *MediaCloudClient) Unmarsal(buff []byte) (protos []IMediaCloudAppProto, err error) {
	Log("unmarsal buff : %s", string(buff))
	protos = make([]IMediaCloudAppProto, 0)

	lenLeft := len(this._bytesLeft)

	newBuf := make([]byte, lenLeft+len(buff))

	copy(newBuf, this._bytesLeft)
	copy(newBuf[lenLeft:], buff)

	this._bytesLeft = newBuf

	for {
		if len(this._bytesLeft) < 6 {
			return protos, nil
		}

		if this._bytesLeft[0] != 0xFA && this._bytesLeft[1] != 0xAF {
			LogError("wrong proto header error!")
			return nil, errors.New("wrong proto header")
		}

		protoLen := int(binary.BigEndian.Uint16(this._bytesLeft[4:]))

		if len(this._bytesLeft) < 6+protoLen {
			return protos, nil
		}

		jsonMap := make(map[string]interface{})

		protoBuf := this._bytesLeft[6 : 6+protoLen]

		err = json.Unmarshal(protoBuf, &jsonMap)

		Log("unmarsal proto : %s ", string(protoBuf))

		if err != nil {
			LogError(err.Error())

			return
		}

		this._bytesLeft = this._bytesLeft[6+protoLen:]

		cmd := jsonMap["cmd"].(string)

		if cmd == "login-resp" {
			loginResp := &MediaAppLoginProto{}
			err = json.Unmarshal(protoBuf, loginResp)

			if err != nil {
				LogError(err.Error())
				return
			}

			protos = append(protos, loginResp)

		} else if cmd == "create-session-resp" {
			createSessionResp := &MediaAppCreateSessionProto{}

			err = json.Unmarshal(protoBuf, createSessionResp)

			if err != nil {
				LogError(err.Error())
				return
			}

			protos = append(protos, createSessionResp)
		}
	}

}

func (this *MediaCloudClient) Marsal(mediaProto IMediaCloudAppProto) []byte {
	protoBuf := mediaProto.Marsal()

	Log("marsal proto : %s ", string(protoBuf))

	buff := make([]byte, 6+len(protoBuf))

	buff[0] = 0xFA
	buff[1] = 0xAF

	binary.BigEndian.PutUint16(buff[4:], uint16(len(protoBuf)))

	copy(buff[6:], protoBuf)

	return buff

}

func doCreateLoginReq(token string, credential string) []byte {
	marsal := make(map[string]interface{})

	marsal["cmd"] = "login"
	marsal["credential"] = credential
	marsal["apptoken"] = token

	jsonbuf, _ := json.Marshal(marsal)

	Log("request : %s", string(jsonbuf))

	buff := make([]byte, len(jsonbuf)+6)
	copy(buff[6:], jsonbuf)

	FillHeader(buff, uint16(len(jsonbuf)))

	Log("dump login req %X", buff)

	return buff
}

type TestSessionConfig struct {
}

func doCreateSessionReq(cmdid string) []byte {

	marsal := make(map[string]interface{})
	config := make(map[string]interface{})

	marsal["cmd"] = "create-session"
	marsal["cmdid"] = cmdid

	jsonStr := "{\"players\":[{\"uid\":\"dy_iPad4,4\",\"screenpos\":\"0.1;0.5;0.5;0.5\"}],\"host\":{\"uid\":\"dy_iPhone7,1\"},\"cdn\":{\"url\":\"rtmp://101.201.146.134/hulu/lianmaitest\"}}"
	jsonbuff := []byte(jsonStr)

	json.Unmarshal(jsonbuff, &config)
	marsal["config"] = config

	jsonbuf, _ := json.Marshal(marsal)

	Log("request : %s", string(jsonbuf))
	buff := make([]byte, len(jsonbuf)+6)
	copy(buff[6:], jsonbuf)

	FillHeader(buff, uint16(len(jsonbuf)))

	Log("dump create session %X", buff)

	return buff
}

func FillHeader(buff []byte, dataLen uint16) {
	buff[0] = 0xFA
	buff[1] = 0xAF

	binary.BigEndian.PutUint16(buff[4:], dataLen)
}
