package main

type LoginUser struct {
	Uid string `json:"uid"`
	Pwd string `json:"pwd"`
}

type User struct {
	Uid   string `json:"uid"`
	Nick  string `json:"nick"`
	State uint8  `json:"state"`
	Pwd   string `json:"-"`
	Token string `json:"-"`
}

type LoginResponse struct {
	Uid       string `json:"uid"`
	Token     string `json:"token"`
	SessionId string `json:"sessionid"`
}
