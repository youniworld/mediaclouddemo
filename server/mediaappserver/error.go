package main

import (
	"encoding/json"
)

var (
	KErrorNone              = &Error{ErrorCode: 0, ErrorDesc: ""}
	KErrorUnauthorizedToken = &Error{ErrorCode: 1, ErrorDesc: "unauthorized token"}
	KErrorPageNotFound      = &Error{ErrorCode: 1000, ErrorDesc: "page not found"}
	KErrorBodyParseError    = &Error{ErrorCode: 1001, ErrorDesc: "wrong body"}
	KErrorPortalNotFound    = &Error{ErrorCode: 1002, ErrorDesc: "portal not found in the header"}
	KErrorPortalInvalid     = &Error{ErrorCode: 1003, ErrorDesc: "portal is invalid"}

	// user
	KErrorUserExisted  = &Error{ErrorCode: 3000, ErrorDesc: "user existed"}
	KErrorUserRegister = &Error{ErrorCode: 3001, ErrorDesc: "user register failed"}

	// session
	KErrorSessionNotFound = &Error{ErrorCode: 4000, ErrorDesc: "session not found"}
)

type Error struct {
	ErrorCode int    `json:"errorcode"`
	ErrorDesc string `json:"errordesc"`
}

func (this *Error) toJson() []byte {

	buff, _ := json.Marshal(this)

	return buff
}
