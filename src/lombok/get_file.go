package lombok

import (
	"github.com/cavaliergopher/grab/v3"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/gzip"
	"os"
	"time"
)

func GetWithinVersion(url string, version string) (string, error) {
	compress := "./tmp/" + version
	path := compress + "tar.gz"
	log.Infof("Start download: %s", url)
	client := grab.NewClient()
	client.UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.33"
	req, _ := grab.NewRequest(path, url)
	resp := client.Do(req)
	for !resp.IsComplete() {
		time.Sleep(time.Second)
		log.Infof("Download %s...  %.2f%", version, resp.Progress()*100)
	}
	log.Infof("Download finish: %s", version)

	err := gzip.DeCompress(path, compress)
	if err != nil {
		return "", err
	}
	open, err := os.Open(compress + "/plugins/lombok")
	if err != nil {
		return "", err
	}
	gzipFile := "./lombok-" + version + ".tar.gz"
	err = gzip.Compress([]*os.File{open}, gzipFile)
	if err != nil {
		return "", err
	}
	return gzipFile, nil
}
