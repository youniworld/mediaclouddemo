package main

import (
	"os"
)

func main() {

	if len(os.Args) > 1 {
		for _, arg := range os.Args {
			if arg == "debug" {
				_Env._debug = true
			} else if arg == "test" {
				_Env._test = true
			}
		}
	}

	_fLogger.init("mediaappserver.log")
	server := &Server{}

	Log("try to start the server ... ")
	server.start()
}
