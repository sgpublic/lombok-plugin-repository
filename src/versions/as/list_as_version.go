package as

import (
	"bytes"
	"github.com/emirpasic/gods/maps/hashmap"
	"github.com/emirpasic/gods/queues/priorityqueue"
	"github.com/emirpasic/gods/utils"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/util"
	"lombok-plugin-action/src/util/web"
	"strconv"
	"strings"
)

type AndroidStudioRelease struct {
	PlatformVersion StrictPlatformVersion `json:"platformVersion"`
	PlatformBuild   string                `json:"platformBuild"`
	Version         string                `json:"version"`
	Name            string                `json:"name"`
	Build           string                `json:"build"`
	Channel         string                `json:"channel"`
}

type StrictPlatformVersion string

func (spv *StrictPlatformVersion) UnmarshalJSON(b []byte) error {
	b = bytes.Trim(b, "\"")
	*spv = StrictPlatformVersion(b)
	return nil
}

type _Products struct {
	Content struct {
		Items []AndroidStudioRelease `json:"item"`
	}
}

func ListVersions() (*priorityqueue.Queue, *hashmap.Map) {
	versions := getJson().Content.Items
	if len(versions) <= 0 {
		util.FatalLogln("empty result of Android Studio versions")
	}

	m := hashmap.New()
	for _, version := range versions {
		if len(strings.Split(version.Version, ".")[0]) < 4 {
			continue
		}
		build, _ := strconv.Atoi(strings.Split(version.PlatformBuild, ".")[0])
		if build <= 202 {
			continue
		}
		value, found := m.Get(version.PlatformBuild)
		if !found {
			value = []AndroidStudioRelease{}
		}

		m.Put(version.PlatformBuild, append(value.([]AndroidStudioRelease), version))
	}

	return sort(m), m
}

func getJson() *_Products {
	as := "https://jb.gg/android-studio-releases-list.json"
	log.Infof("Getting Google Android Studio versions from %s", as)
	resp := &_Products{}
	web.GetJson(as, resp)
	return resp
}

func byPriority(a, b interface{}) int {
	return utils.StringComparator(a, b)
}

func sort(m *hashmap.Map) *priorityqueue.Queue {
	queue := priorityqueue.NewWith(byPriority)
	for _, item := range m.Keys() {
		queue.Enqueue(item)
	}
	return queue
}
