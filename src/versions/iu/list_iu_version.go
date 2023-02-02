package iu

import (
	"github.com/emirpasic/gods/maps/hashmap"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/util"
	"lombok-plugin-action/src/util/web"
)

type _Products []struct {
	Release []struct {
		Downloads struct {
			WindowsZip struct {
				Link         string `json:"link"`
				Size         int    `json:"size"`
				ChecksumLink string `json:"checksumLink"`
			} `json:"windowsZip,omitempty"`
		} `json:"downloads"`
		Build string `json:"build"`
	} `json:"releases"`
}

func ListVersions() *hashmap.Map {
	release := (*getJson())[0].Release
	if len(release) <= 0 {
		util.FatalLogln("empty result of IntelliJ IDEA Ultimate versions")
	}

	m := hashmap.New()

	for _, version := range release {
		m.Put(version.Build, version.Downloads.WindowsZip.Link)
	}

	return m
}

func getJson() *_Products {
	iu := "https://data.services.jetbrains.com/products?release.type=release&code=IU&fields=releases"
	log.Infof("Getting JetBrains IntelliJ IDEA versions from %s", iu)
	resp := &_Products{}
	web.GetJson(iu, resp)
	return resp
}
