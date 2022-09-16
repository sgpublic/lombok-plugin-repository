package as

import (
	"github.com/PuerkitoBio/goquery"
	"github.com/emirpasic/gods/maps/hashmap"
	"github.com/emirpasic/gods/queues/priorityqueue"
	"github.com/emirpasic/gods/utils"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/web"
	"regexp"
	"strings"
)

func ListVersions() (*priorityqueue.Queue, *hashmap.Map) {
	achieve := getAchieveUrl()
	return getFromAchieve(achieve)
}

func getAchieveUrl() string {
	as := "https://developer.android.com/studio/archive"
	doc := web.GetDoc(as)
	var achieve = "nil"
	doc.Find("iframe").Each(func(i int, selection *goquery.Selection) {
		if achieve != "nil" {
			return
		}
		achieve = selection.AttrOr("src", "nil")
	})
	if achieve == "nil" {
		log.Fatalf("cannot find iframe on %s", as)
	}
	return "https://developer.android.com" + achieve
}

func getFromAchieve(url string) (*priorityqueue.Queue, *hashmap.Map) {
	result := hashmap.New()

	doc := web.GetDoc(url)
	reg, _ := regexp.Compile(`\d{4}\.\d+\.\d+`)
	doc.Find(".all-downloads").Find(".expand-control").Each(func(i int, selection *goquery.Selection) {
		data := selection.Text()
		data = strings.Split(data, "\n")[0]
		data = strings.TrimSpace(data)
		version := reg.FindString(data)
		if version != "" {
			log.Debugf("Version recognized: %s", data)
			value, _ := result.Get(version)
			if value == nil {
				value = []string{}
			}
			value = append(value.([]string), data)
			result.Put(version, value)
		}
	})
	if result.Empty() {
		log.Fatalf("failed to get versions of Android Studio")
	}

	return sort(result), result
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
