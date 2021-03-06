package main

import (
	"encoding/binary"
	"errors"

	proto "github.com/golang/protobuf/proto"
)

type ISessionProto interface {
	FromBuff(buff []byte)
	ToBuff() []byte
	ProtoBuf() *MediaAppSignalMessage
}

type ProtBase struct {
	_proto *MediaAppSignalMessage
}

type LoginProto struct {
	ProtBase
	_user     string
	_pwd      string
	_token    string
	_portal   string
	_success  bool
	_reason   string
	_packetId uint64
}

func (this *LoginProto) FromBuff(buff []byte) {

}

func (this *LoginProto) ToBuff() []byte {
	var state uint32
	state = 0
	message := &MediaAppSignalMessage{}

	message.XSignal = &MediaSignalMessage{}
	message.XSignal.XLoginResp = &MediaSignalMessage_LoginResp{}
	message.XSignal.XLoginResp.XToken = &this._token
	message.XSignal.XLoginResp.XCode = &state
	message.XBase = &MediaAppSignalMessage_MediaMessageBase{}
	message.XBase.XPacketId = &this._packetId

	this._proto = message

	if !this._success {
		state = 1
		message.XSignal.XLoginResp.XCode = &state
	}

	message.XSignal.XLoginResp.XFailedReason = &this._reason

	buff, _ := proto.Marshal(message)

	return buff
}

func (this *LoginProto) ProtoBuf() *MediaAppSignalMessage {
	return this._proto
}

type LogoutProto struct {
	ProtBase
	_user string
}

func (this *LogoutProto) FromBuff(buff []byte) {
}

func (this *LogoutProto) ToBuff() []byte {
	return nil
}

func (this *LogoutProto) ProtoBuf() *MediaAppSignalMessage {
	return this._proto
}

type StateProto struct {
	ProtBase
	_user  string
	_state uint8
}

func (this *StateProto) FromBuff(buff []byte) {

}

func (this *StateProto) ToBuff() []byte {
	message := &MediaAppSignalMessage{}
	message.XSignal = &MediaSignalMessage{}
	message.XSignal.XStateChanged = &MediaSignalMessage_OnlineStateChange{}

	message.XSignal.XStateChanged.XUid = &this._user

	var state uint32

	state = uint32(this._state)

	message.XSignal.XStateChanged.XState = &state

	this._proto = message
	buff, _ := proto.Marshal(message)

	return buff
}

func (this *StateProto) ProtoBuf() *MediaAppSignalMessage {
	return this._proto
}

type PingProto struct {
	ProtBase
	_packetId uint64
}

func (this *PingProto) FromBuff(buff []byte) {

}

func (this *PingProto) ToBuff() []byte {
	message := &MediaAppSignalMessage{}
	message.XSignal = &MediaSignalMessage{}
	message.XSignal.XPong = &MediaSignalMessagePong{}
	message.XBase = &MediaAppSignalMessage_MediaMessageBase{}
	message.XBase.XPacketId = &this._packetId

	this._proto = message
	buff, _ := proto.Marshal(message)

	return buff
}

func (this *PingProto) ProtoBuf() *MediaAppSignalMessage {
	return this._proto
}

type CallProto struct {
	ProtBase
}

func (this *CallProto) FromBuff(buff []byte) {

}

func (this *CallProto) ToBuff() []byte {
	buff, _ := proto.Marshal(this._proto)

	return buff
}

func (this *CallProto) ProtoBuf() *MediaAppSignalMessage {
	return this._proto
}

type ProtoParser struct {
	_bytesLeft []byte
}

func (this *ProtoParser) UnMarsal(buff []byte) (protos []ISessionProto, err error) {

	leftlen := len(this._bytesLeft)
	newbuf := make([]byte, leftlen+len(buff))

	copy(newbuf, this._bytesLeft)
	copy(newbuf[leftlen:], buff)

	this._bytesLeft = newbuf

	protos = make([]ISessionProto, 0)

	for {
		if len(this._bytesLeft) < 6 {
			return protos, nil
		}

		if this._bytesLeft[0] != 0xFA && this._bytesLeft[1] != 0xAF {
			LogError("proto header error")

			return nil, errors.New("proto header error")
		}

		protolen := binary.BigEndian.Uint32(this._bytesLeft[2:])

		Log("the proto len : %s", protolen)

		if len(this._bytesLeft) < 6+int(protolen) {
			return protos, nil
		}

		message := &MediaAppSignalMessage{}

		err = proto.Unmarshal(this._bytesLeft[6:6+int(protolen)], message)

		if err != nil {
			LogError(err.Error())
			return
		}

		Log("recv message : %s", message.String())

		if message.GetXSignal() != nil {
			if message.GetXSignal().GetXLogin() != nil {
				protologin := &LoginProto{}

				login := message.GetXSignal().GetXLogin()
				protologin._packetId = message.GetXBase().GetXPacketId()

				protologin._user = login.GetXUid()
				protologin._pwd = login.GetXPwd()
				protologin._portal = login.GetXPortal()

				protos = append(protos, protologin)
			} else if message.GetXSignal().GetXLogout() != nil {
				protologout := &LogoutProto{}
				protos = append(protos, protologout)

			} else if message.GetXSignal().GetXPing() != nil {

				protoping := &PingProto{}
				protoping._packetId = message.GetXBase().GetXPacketId()

				protos = append(protos, protoping)
			}
		} else if message.GetXCall() != nil {
			call := &CallProto{}

			call._proto = message

			protos = append(protos, call)
		}

		this._bytesLeft = this._bytesLeft[6+int(protolen):]
	}

	return nil, nil
}

func (this *ProtoParser) Marsal(proto ISessionProto) []byte {
	protobuf := proto.ToBuff()

	if proto.ProtoBuf() != nil {
		Log("send message : %s", proto.ProtoBuf().String())
	}

	buff := make([]byte, 6+len(protobuf))

	buff[0] = 0xFA
	buff[1] = 0xAF

	binary.BigEndian.PutUint32(buff[2:], uint32(len(protobuf)))

	copy(buff[6:], protobuf)
	return buff

}
