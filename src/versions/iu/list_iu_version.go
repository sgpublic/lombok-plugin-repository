package iu

import (
	"github.com/emirpasic/gods/maps/hashmap"
	"github.com/emirpasic/gods/queues/priorityqueue"
	"github.com/emirpasic/gods/utils"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/util"
	"lombok-plugin-action/src/util/web"
)

type IdeaRelease struct {
	Downloads struct {
		WindowsZip struct {
			Link         string `json:"link"`
			Size         int    `json:"size"`
			ChecksumLink string `json:"checksumLink"`
		} `json:"windowsZip,omitempty"`
	} `json:"downloads"`
	Build string `json:"build"`
	Date  string `json:"date"`
}

type _Products []struct {
	Release []IdeaRelease `json:"releases"`
}

func ListVersions() (*priorityqueue.Queue, *hashmap.Map) {
	release := (*getJson())[0].Release
	if len(release) <= 0 {
		util.FatalLogln("empty result of IntelliJ IDEA Ultimate versions")
	}

	m := hashmap.New()

	for _, version := range release {
		m.Put(version.Build, version)
	}

	return sort(m), m
}

func getJson() *_Products {
	iu := "https://data.services.jetbrains.com/products?code=IU&fields=releases"
	log.Infof("Getting JetBrains IntelliJ IDEA versions from %s", iu)
	resp := &_Products{}
	web.GetJson(iu, resp)
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
