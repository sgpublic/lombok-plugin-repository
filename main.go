package main

import (
	"github.com/emirpasic/gods/maps/hashmap"
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
	"strings"
	"time"
)

func init() {
	config.Setup()
	github.Init()
}

func main() {
	jar, _ := cookiejar.New(nil)
	http.DefaultClient = &http.Client{
		Jar: jar,
	}

	// cron mode
	if config.Cron() == "" || config.IsNow() {
		doAction()
		return
	} else {
		util.KeepWhenException = true
	}

	c := cron3.New()
	_ = c.AddFunc(config.Cron(), doAction)
	log.Infof("lombok-plugin-action started!")
	c.Run()
}

func doAction() {
	log.Info("Start updating lombok plugin...")

	iuVer, iuInfo := iu.ListVersions()
	asVer, asInfo := as.ListVersions()
	log.Infof("Android Studio versions (%d in total):", asVer.Size())
	var item interface{}
	var hasNext bool
	needUpdate := false
	sizes := hashmap.New()
	for {
		item, hasNext = asVer.Dequeue()
		if !hasNext {
			break
		}

		verTag := item.(string)
		if !config.IsDebug() {
			log.Infoln("Sleep 10 second...")
			time.Sleep(time.Second * 10)
		}

		verList, _ := asInfo.Get(item)
		verInfo := verList.([]as.AndroidStudioRelease)

		var verNames []string
		for _, version := range verInfo {
			verNames = append(verNames, version.Name)
		}

		log.Infof("- Platform Version %s:\n  > %s", verTag, strings.Join(verNames, "\n  > "))

		release, err := github.GetReleaseByTag(verTag)
		if err == nil {
			log.Infof("Tag of %s already exits, updateing...", verTag)
			var note string
			var prerelease bool
			if len(release.Assets) > 0 {
				sizes.Put(verTag, *release.Assets[0].Size)
				note, prerelease = lombok.CreateReleaseNote(verInfo)
			} else {
				note, prerelease = lombok.CreateEmptyReleaseNote(verInfo)
			}
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
			err = github.CreateEmptyTag(verTag, verInfo)
			if err != nil {
				return
			}
			continue
		}

		zipFile, err := lombok.GetVersion(info.(iu.IdeaRelease).Downloads.WindowsZip.Link, verTag)
		if err != nil {
			log.Errorf("Failed to get version %s: %v", verTag, err)
			continue
		}
		stat, err := os.Stat(zipFile)
		if err != nil {
			log.Errorf("Failed to get version %s: %v", verTag, err)
			continue
		}
		sizes.Put(verTag, int(stat.Size()))

		err = github.CreateTag(verTag, verInfo, zipFile)
		if err != nil {
			log.Errorf("Failed to upload version %s: %v", verTag, err)
		} else {
			needUpdate = true
			log.Infof("Version %s upload finish.", verTag)
		}
	}
	xml, err := plugin.CreateRepositoryXml(iuVer, iuInfo, sizes)
	if err != nil {
		log.Warnf("Creating plugin repository failed, %v", err)
		return
	}

	err = github.CreatePluginRepository(xml, needUpdate)
	if err != nil {
		log.Warnf("Updating plugin repository failed, %v", err)
	} else {
		log.Info("Updating plugin repository success!")
	}
	log.Info("Updating lombok plugin finish!")
}
