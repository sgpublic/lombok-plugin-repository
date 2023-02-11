package util

import (
	log "github.com/sirupsen/logrus"
	"os"
)

var KeepWhenException = false

func FatalLogf(format string, args ...interface{}) {
	log.Errorf(format, args...)
	checkExit()
}

func FatalLogln(args ...interface{}) {
	log.Errorln(args...)
	checkExit()
}

func checkExit() {
	if !KeepWhenException {
		os.Exit(1)
	}
}
