package main

import (
	"flag"
	"github.com/emirpasic/gods/maps/hashmap"
	rotatelogs "github.com/lestrrat-go/file-rotatelogs"
	"github.com/mattn/go-colorable"
	cron3 "github.com/robfig/cron"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/config"
	"lombok-plugin-action/src/git/github"
	"lombok-plugin-action/src/lombok"
	"lombok-plugin-action/src/util"
	"lombok-plugin-action/src/versions/as"
	"lombok-plugin-action/src/versions/iu"
	"lombok-plugin-action/src/versions/plugin"
	"net/http"
	"net/http/cookiejar"
	"os"
	"os/exec"
	"path"
	"path/filepath"
	"strings"
	"time"
)

var (
	service = false
	cron    = "" // 0 * 2 * * *
	debug   = false
)

func init() {
	initFlag()
	initLogrus()
	github.Init()
}

func main() {
	// daemon mode
	if service {
		args := os.Args[1:]
		execArgs := make([]string, 0)
		l := len(args)
		cronEnable := false
		for i := 0; i < l; i++ {
			if strings.Compare(args[i], "-service") == 0 {
				continue
			}
			if strings.Compare(args[i], "-cron") == 0 {
				cronEnable = true
			}
			execArgs = append(execArgs, args[i])
		}
		if !cronEnable {
			execArgs = append(execArgs, "-cron", "0 0 2 * * *")
		}

		ex, _ := os.Executable()
		p, _ := filepath.Abs(ex)
		proc := exec.Command(p, execArgs...)
		err := proc.Start()
		if err != nil {
			panic(err)
		}
		log.Infof("[PID] %d", proc.Process.Pid)
		os.Exit(0)
	}

	jar, _ := cookiejar.New(nil)
	http.DefaultClient = &http.Client{
		Jar: jar,
	}

	// cron mode
	if strings.Compare(cron, "") == 0 {
		doAction()
		return
	} else {
		config.KeepWhenException = true
	}

	c := cron3.New()
	c.AddFunc(cron, doAction)
	log.Infof("lombok-plugin-action started!")
	c.Run()
}

func initFlag() {
	flag.StringVar(&github.TOKEN, "token", "", "Github Security Token")
	flag.StringVar(&github.REPO, "repo", "", "Target repo")
	flag.BoolVar(&debug, "debug", false, "Debug mod")
	flag.BoolVar(&service, "service", false, "Service mod")
	flag.StringVar(&cron, "cron", "", "Crontab operation")
	flag.Parse()
}

func initLogrus() {
	log.SetOutput(colorable.NewColorableStdout())
	log.SetFormatter(util.LogFormat{EnableColor: true})

	log.RegisterExitHandler(func() {
		_ = os.RemoveAll("/tmp/lombok-plugin/")
	})

	rotateOptions := []rotatelogs.Option{
		rotatelogs.WithRotationTime(time.Hour * 24),
	}
	rotateOptions = append(rotateOptions, rotatelogs.WithMaxAge(259200*time.Second))
	err := os.MkdirAll("/var/log/lombok/", 0744)
	if err != nil && !os.IsExist(err) {
		log.Errorf("log dir init err: %v", err)
		return
	}
	w, err := rotatelogs.New(path.Join("/var/log/lombok/", "%Y-%m-%d.log"), rotateOptions...)
	if err != nil {
		log.Errorf("rotatelogs init err: %v", err)
	} else {
		log.AddHook(util.NewLocalHook(w, debug))
	}

	// debug mode
	if debug {
		log.SetLevel(log.DebugLevel)
		log.Debug("Enable debug mode!")
	} else {
		log.SetLevel(log.InfoLevel)
	}
}

func doAction() {
	log.Info("Start updating lombok plugin...")

	err := os.MkdirAll("/tmp/lombok-plugin", 0744)
	if err != nil && !os.IsExist(err) {
		log.Warnf("Create temp dir failed: %s", err.Error())
		return
	}

	iuVer, iuInfo := iu.ListVersions()
	asVer, asInfo := as.ListVersions()
	log.Infof("Android Studio versions (%d in total):", asVer.Size())
	var item interface{}
	var hasNext bool
	needUpdate := false
	sizes := hashmap.New()
	for {
		log.Infoln("Sleep 10 second...")
		time.Sleep(time.Second * 10)
		item, hasNext = asVer.Dequeue()
		if !hasNext {
			break
		}

		verTag := item.(string)
		verList, _ := asInfo.Get(item)
		verInfo := verList.([]as.AndroidStudioRelease)

		var verNames []string
		for _, version := range verInfo {
			verNames = append(verNames, version.Name)
		}

		log.Infof("- Platform Version %s:\n  > %s", verTag, strings.Join(verNames, "\n  > "))

		release, err := github.GetReleaseByTag(verTag)
		if err == nil {
			sizes.Put(verTag, *release.Assets[0].Size)
			log.Infof("Tag of %s already exits, updateing...", verTag)
			note, prerelease := lombok.CreateReleaseNote(verInfo)
			if release.GetBody() == note && *release.Prerelease == prerelease {
				log.Warnf("Tag of %s is up to date, skip.", verTag)
				continue
			}
			release.Body = &note
			release.Prerelease = &prerelease
			err = github.UpdateReleaseBody(release)
			if err != nil {
				log.Warnf("Tag of %s update failed.", verTag)
			} else {
				needUpdate = true
				log.Warnf("Tag of %s update success.", verTag)
			}
			continue
		}

		info, found := iuInfo.Get(item)
		if !found {
			log.Warnf("Version %s exists in Android Studio, but not exists in IDEA.", verTag)
			continue
		}

		zipFile, err := lombok.GetVersion(info.(iu.IdeaRelease).Downloads.WindowsZip.Link, verTag)
		stat, err := os.Stat(zipFile)
		if err != nil {
			log.Errorf("Failed to get version %s: %s", verTag, err.Error())
			continue
		}
		sizes.Put(verTag, stat.Size())
		err = github.CreateTag(verTag, verInfo, zipFile)
		if err != nil {
			log.Errorf("Failed to upload version %s: %s", verTag, err.Error())
		} else {
			needUpdate = true
			log.Infof("Version %s upload finish.", verTag)
		}
	}
	xml, err := plugin.CreateRepositoryXml(iuVer, iuInfo, sizes)
	if err != nil {
		log.Warnf("Creating plugin repository failed, %s", err.Error())
		return
	}
	err = github.CreatePluginRepository(xml, needUpdate)
	if err != nil {
		log.Warnf("Updating plugin repository failed, %s", err.Error())
	} else {
		log.Info("Updating plugin repository success!")
	}
	log.Info("Updating lombok plugin finish!")
}
