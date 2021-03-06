package main

import (
	"encoding/binary"
	"errors"

	proto "github.com/golang/protobuf/proto"
)

type ISessionProto interface {
	FromBuff(buff []byte)
	ToBuff() []byte
}

type LoginProto struct {
	_user   string
	_pwd    string
	_token  string
	_portal string
}

func (this *LoginProto) FromBuff(buff []byte) {

}

func (this *LoginProto) ToBuff() []byte {
	message := &MediaAppSignalMessage{}

	message.XSignal = &MediaSignalMessage{}
	message.XSignal.XLogin = &MediaSignalMessage_Login{}
	message.XSignal.XLogin.XPortal = &this._portal
	message.XSignal.XLogin.XUid = &this._user
	message.XSignal.XLogin.XPwd = &this._pwd

	buff, _ := proto.Marshal(message)

	return buff
}

type LogoutProto struct {
	_user string
}

func (this *LogoutProto) FromBuff(buff []byte) {

}

func (this *LogoutProto) ToBuff() []byte {
	return nil
}

type StateProto struct {
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

	buff, _ := proto.Marshal(message)

	return buff
}

type PingProto struct {
}

func (this *PingProto) FromBuff(buff []byte) {

}

func (this *PingProto) ToBuff() []byte {
	message := &MediaAppSignalMessage{}
	message.XSignal = &MediaSignalMessage{}
	message.XSignal.XPing = &MediaSignalMessagePing{}

	buff, _ := proto.Marshal(message)

	return buff
}

type CallProto struct {
	_proto *MediaAppSignalMessage
}

func (this *CallProto) FromBuff(buff []byte) {

}

func (this *CallProto) ToBuff() []byte {
	return nil
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

		if 6+int(protolen) < len(this._bytesLeft) {
			return protos, nil
		}

		message := &MediaAppSignalMessage{}

		err = proto.Unmarshal(this._bytesLeft[6:], message)

		if err != nil {
			LogError(err.Error())
			return
		}

		Log("recv message : %s", message.String())

		if message.GetXSignal() != nil {
			if message.GetXSignal().GetXLoginResp() != nil {
				protoResp := &LoginProto{}

				loginResp := message.GetXSignal().GetXLoginResp()

				protoResp._token = loginResp.GetXToken()

				protos = append(protos, protoResp)
			} else if message.GetXSignal().GetXPong() != nil {
				protoping := &PingProto{}

				protos = append(protos, protoping)
			} else if message.GetXSignal().GetXStateChanged() != nil {
				stateChange := message.GetXSignal().GetXStateChanged()

				stateChange.String()
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

	buff := make([]byte, 6+len(protobuf))

	buff[0] = 0xFA
	buff[1] = 0xAF

	binary.BigEndian.PutUint32(buff[2:], uint32(len(protobuf)))

	copy(buff[6:], protobuf)

	return buff

}
