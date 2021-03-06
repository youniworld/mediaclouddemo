package main

import (
	"fmt"
	"io"
	"os"
	"time"
)

const (
	KSize1K = 1024
	KSize1M = 1024 * KSize1K
)

func Log(format string, a ...interface{}) {
	prefix := fmt.Sprintf("[%s]  ", time.Now().Format("15:04:05.000"))
	prefix += fmt.Sprintf(format+"\r\n", a...)
	fmt.Print(prefix)

	if _fLogger._init {
		_fLogger.log(prefix)
	}

}

func LogError(format string, a ...interface{}) {
	Log("[ERROR] "+format, a...)
}

func LogWarn(format string, a ...interface{}) {
	Log("[WARN] "+format, a...)
}

type FileLogger struct {
	_fileName string
	_init     bool
	_ch       chan string
}

var _fLogger = &FileLogger{}

func (this *FileLogger) init(fileName string) error {
	if this._init {
		os.Remove(this._fileName)
	}

	this._ch = make(chan string)
	this._fileName = fileName

	CreateIfNotExist(fileName)

	go MessageHandler(this)

	this._init = true

	return nil
}

func (this *FileLogger) log(message string) {
	this._ch <- message
}

func MessageHandler(logger *FileLogger) {
	for {
		select {
		case str := <-logger._ch:
			fileName := logger._fileName
			info, err := os.Stat(logger._fileName)

			if err != nil {
				if info.Size() > 2*KSize1M {
					fileName = fmt.Sprintf("%s_%s", fileName, time.Now().Format("2006-01-02 15:04:05"))

					des := CreateIfNotExist(fileName)

					if des != nil {
						src, err := os.OpenFile(logger._fileName, os.O_APPEND|os.O_RDWR, 0)

						if err == nil {
							io.Copy(des, src)
							src.Close()

							os.Remove(logger._fileName)

							out := CreateIfNotExist(logger._fileName)

							out.Close()
						}

						des.Close()
					}

				}
			}

			in, err := os.OpenFile(fileName, os.O_APPEND|os.O_RDWR, 0)

			if err == nil {
				in.WriteString(str)
			} else {
				fmt.Println(err.Error())
			}

			in.Close()
		}
	}
}

func CreateIfNotExist(file string) *os.File {
	_, err := os.Stat(file)

	exist := false
	if err == nil || os.IsExist(err) {
		exist = true
	}

	var f *os.File

	if !exist {
		f, err = os.Create(file)
	} else {
		f, err = os.Open(file)
	}

	return f
}
