package main

import (
	"fmt"
	"io"
	"os"
	"time"
)

var _LogLevel = -1

const (
	KSize1K = 1024
	KSize1M = 1024 * KSize1K
)

const (
	KLogLevelDebug = iota
	KLogLevelInfo
	KLogLevelWarn
	KLogLevelError
)

func LogLevel(level int, format string, a ...interface{}) {

	if level > _LogLevel {
		LogAny(format, a...)
	}
}

func Log(format string, a ...interface{}) {
	LogLevel(KLogLevelDebug, "[DEBUG] "+format, a...)
}

func LogAny(format string, a ...interface{}) {
	prefix := fmt.Sprintf("[%s]  ", time.Now().Format("15:04:05.000"))
	prefix += fmt.Sprintf(format+"\r\n", a...)
	fmt.Print(prefix)

	if _fLogger._init {
		_fLogger.log(prefix)
	}
}

func LogInfo(format string, a ...interface{}) {
	LogLevel(KLogLevelInfo, "[ERROR] "+format, a...)
}

func LogError(format string, a ...interface{}) {
	LogLevel(KLogLevelError, "[ERROR] "+format, a...)
}

func LogWarn(format string, a ...interface{}) {
	LogLevel(KLogLevelWarn, "[WARN] "+format, a...)
}

type FileLogger struct {
	_fileName string
	_init     bool
	_ch       chan string
}

var _fLogger = &FileLogger{}

func (this *FileLogger) init(fileName string) error {
	if this._init {
		//os.Remove(this._fileName)
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
	defer func() {
		if err := recover(); err != nil {
			fmt.Println(err)
		}
	}()

	for {
		select {
		case str := <-logger._ch:
			fileName := logger._fileName
			info, err := os.Stat(logger._fileName)

			if err == nil {
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

			fileName = logger._fileName

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
