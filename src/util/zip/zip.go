package zip

import (
	"archive/zip"
	log "github.com/sirupsen/logrus"
	"io"
	"os"
	"path/filepath"
	"strings"
)

func DeCompress(zipFile string, prefix string, dest string) (err error) {
	log.Infof("DeCompressing %s...", zipFile)
	zr, err := zip.OpenReader(zipFile)
	defer zr.Close()
	if err != nil {
		return
	}

	if dest != "" {
		if err := os.MkdirAll(dest, 0755); err != nil {
			return err
		}
	}

	for _, file := range zr.File {
		path := filepath.Join(dest, file.Name)

		if file.FileInfo().IsDir() {
			if err := os.MkdirAll(path, file.Mode()); err != nil {
				return err
			}
			continue
		}

		if err := os.MkdirAll(filepath.Dir(path), file.Mode()); err != nil {
			return err
		}

		if !strings.HasPrefix(path, prefix) {
			continue
		}
		log.Infof("decompressing: %s", file.Name)

		fr, err := file.Open()
		if err != nil {
			return err
		}

		fw, err := os.OpenFile(path, os.O_CREATE|os.O_RDWR|os.O_TRUNC, file.Mode())
		if err != nil {
			return err
		}

		_, err = io.Copy(fw, fr)
		if err != nil {
			return err
		}

		_ = fw.Close()
		_ = fr.Close()
	}
	log.Infof("DeCompress finish: %s", zipFile)
	return nil
}
