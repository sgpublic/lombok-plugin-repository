package zip

import (
	"archive/zip"
	"github.com/mholt/archiver/v4"
	log "github.com/sirupsen/logrus"
	"golang.org/x/net/context"
	"io"
	"os"
	"path"
	"path/filepath"
	"strings"
)

func Compress(files string, prefix string, name string) error { // map files on disk to their paths in the archive
	zip, err := zipFile(files, prefix)
	if err != nil {
		return err
	}

	// create the output file we'll write to
	out, err := os.Create(name)
	if err != nil {
		return err
	}
	defer out.Close()

	format := archiver.CompressedArchive{
		Archival: archiver.Zip{},
	}

	err = format.Archive(context.Background(), out, zip)
	return err
}

func zipFile(file string, prefix string) ([]archiver.File, error) {
	log.Debugf("zipFile(\"%s\", \"%s\")", file, prefix)
	stat, err := os.Stat(file)
	if err != nil {
		return nil, err
	}
	if !stat.IsDir() {
		return archiver.FilesFromDisk(nil, map[string]string{
			file: prefix,
		})
	}

	var next []archiver.File
	dirs, err := os.ReadDir(file)
	if err != nil {
		return nil, err
	}
	for _, dir := range dirs {
		files, err := zipFile(file+"/"+dir.Name(), prefix+"/"+dir.Name())
		if err != nil {
			return nil, err
		}
		next = append(next, files...)
	}
	return next, nil
}

func DeCompress(zipPath string, prefix string, dstDir string) error {
	log.Infof("DeCompressing %s...", zipPath)
	// open zip file
	reader, err := zip.OpenReader(zipPath)
	if err != nil {
		return err
	}
	defer reader.Close()
	for _, file := range reader.File {
		if !strings.HasPrefix(file.Name, prefix) {
			continue
		}
		if err := unzipFile(file, dstDir); err != nil {
			return err
		}
	}
	log.Infof("DeCompress finish: %s", zipPath)
	return nil
}

func unzipFile(file *zip.File, dstDir string) error {
	// create the directory of file
	filePath := path.Join(dstDir, file.Name)
	log.Infof("decompressing: %s", file.Name)
	if file.FileInfo().IsDir() {
		if err := os.MkdirAll(filePath, os.ModePerm); err != nil {
			return err
		}
		return nil
	}
	if err := os.MkdirAll(filepath.Dir(filePath), os.ModePerm); err != nil {
		return err
	}

	// open the file
	rc, err := file.Open()
	if err != nil {
		return err
	}
	defer rc.Close()

	// create the file
	w, err := os.Create(filePath)
	if err != nil {
		return err
	}
	defer w.Close()

	// save the decompressed file content
	_, err = io.Copy(w, rc)
	return err
}
