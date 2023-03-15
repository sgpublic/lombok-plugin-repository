package lombok

import (
	"github.com/cavaliergopher/grab/v3"
	log "github.com/sirupsen/logrus"
	"lombok-plugin-action/src/config"
	"lombok-plugin-action/src/util/zip"
	"lombok-plugin-action/src/versions/as"
	"time"
)

func GetVersion(url string, version string) (string, error) {
	compress := config.TempDir() + version
	path := compress + "/ideaU.zip"

	log.Infof("Start download: %s", url)
	client := grab.NewClient()
	client.UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.33"
	var req *grab.Request
	var resp *grab.Response
	var err error
	retry := -1
download:
	retry = retry + 1
	req, _ = grab.NewRequest(path, url)
	resp = client.Do(req)
	for !resp.IsComplete() {
		time.Sleep(time.Second * 5)
		log.Infof("Download %s...  %.2f%%", version, resp.Progress()*100)
	}
	if err = resp.Err(); err != nil {
		if retry < 5 {
			goto download
		}
		return "", err
	}

	log.Infof("Download finish: %s", version)
	prefix := "plugins/lombok"
	err = zip.DeCompress(path, prefix, compress)
	if err != nil {
		log.Debug("DeCompress error")
		return "", err
	}
	zipFile := config.TempDir() + "lombok-" + version + ".zip"

	err = zip.Compress(compress+"/"+prefix, "lombok", zipFile)
	if err != nil {
		log.Debug("Compress error")
		return "", err
	}
	return zipFile, nil
}

func CreateReleaseNote(versions []as.AndroidStudioRelease) (string, bool) {
	result := "Applies to the following versions:"
	prerelease := true

	for _, version := range versions {
		result += "\n+ " + version.Name
		if prerelease && ("Patch" == version.Channel || "Release" == version.Channel) {
			prerelease = false
		}
	}
	return result, prerelease
}
