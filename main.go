package main

import (
	"flag"
	"github.com/mattn/go-colorable"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/as"
	"lombok-plugin-action/src/formater"
	"lombok-plugin-action/src/git"
	"lombok-plugin-action/src/iu"
	"lombok-plugin-action/src/lombok"
	"strings"
)

func init() {
	initFlag()
	initLogrus()
	git.Init()
}

func main() {
	doAction()
}

func initFlag() {
	flag.StringVar(&git.TOKEN, "token", "", "Security Token")
	flag.StringVar(&git.REPO, "repo", "", "Target repo")
	debug := false
	flag.BoolVar(&debug, "d", false, "Debug mod")
	flag.Parse()
	if debug {
		log.SetLevel(log.DebugLevel)
	} else {
		log.SetLevel(log.InfoLevel)
	}
}

func initLogrus() {
	log.SetOutput(colorable.NewColorableStdout())
	log.SetFormatter(formater.LogFormat{EnableColor: true})
	log.RegisterExitHandler(func() {
		//_ = os.RemoveAll("./tmp")
	})
}

func doAction() {
	iuVer := iu.ListVersions()
	asVer, info := as.ListVersions()
	log.Infof("Android Studio versions (%d in total):", asVer.Size())
	var item interface{}
	var hasNext bool
	for {
		item, hasNext = asVer.Dequeue()
		if !hasNext {
			break
		}

		verTag := item.(string)
		verStr, _ := info.Get(item)
		verNames := verStr.([]string)

		log.Infof("- %s:\n%s", verTag, strings.Join(verNames, "\n  > "))

		if git.HasTag(verTag) {
			log.Infof("  Tag of %s already exits, skip.", verTag)
			continue
		}

		url, _ := iuVer.Get(item)
		if url == nil {
			log.Warnf("  WARN! Version %s exists in Android Studio, but not exists in IDEA.", verTag)
			continue
		}

		gzipFile, err := lombok.GetWithinVersion(url.(string), item.(string))
		if err != nil {
			log.Errorf("  Failed to get version %s: %s", verTag, err.Error())
			continue
		}
		if git.CreateTag(verTag, verNames, gzipFile) != nil {
			log.Errorf("  Failed to upload version %s: %s", verTag, err.Error())
		} else {
			log.Infof("  Version %s upload finish.", verTag)
		}
	}
}
