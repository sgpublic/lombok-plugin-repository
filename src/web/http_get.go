package web

import (
	"encoding/json"
	"github.com/PuerkitoBio/goquery"
	log "github.com/sirupsen/logrus"
	"io"
	"net/http"
)

func GetDoc(url string) *goquery.Document {
	resp := getResp(url)
	defer resp.Body.Close()
	doc, err := goquery.NewDocumentFromReader(resp.Body)
	if err != nil {
		log.Fatalf("failed to parse %s: %s", url, err.Error())
	}
	return doc
}

func GetJson(url string, v any) {
	resp := getResp(url)
	bytes, err := io.ReadAll(resp.Body)
	_ = resp.Body.Close()
	if err != nil {
		log.Fatalf("failed to read %s: %s", url, err.Error())
	}
	err = json.Unmarshal(bytes, v)
	if err != nil {
		log.Fatalf("failed to parse %s: %s", url, err.Error())
	}
}

func getResp(url string) *http.Response {
	resp, err := http.Get(url)
	if err != nil || resp.StatusCode != 200 {
		log.Fatalf("failed to get %s: %s", url, err.Error())
	}
	return resp
}
