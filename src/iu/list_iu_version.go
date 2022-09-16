package iu

import (
	"github.com/emirpasic/gods/maps/hashmap"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/web"
)

type Products []struct {
	Release []struct {
		Downloads struct {
			Linux struct {
				Link string `json:"link"`
			} `json:"linux,omitempty"`
		} `json:"downloads"`
		Version string `json:"version"`
	} `json:"releases"`
}

func ListVersions() *hashmap.Map {
	release := (*getJson())[0].Release
	if len(release) <= 0 {
		log.Fatal("empty result of IntelliJ IDEA Ultimate versions")
	}

	m := hashmap.New()

	for _, item := range release {
		m.Put(item.Version, item.Downloads.Linux.Link)
	}

	return m
}

func getJson() *Products {
	iu := "https://data.services.jetbrains.com/products?release.type=release&code=IU&fields=releases"
	resp := &Products{}
	web.GetJson(iu, resp)
	return resp
}
