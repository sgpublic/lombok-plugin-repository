package web

import (
	"encoding/json"
	"github.com/PuerkitoBio/goquery"
	"io"
	"lombok-plugin-action/src/util"
	"net/http"
)

func GetDoc(url string) *goquery.Document {
	resp := getResp(url)
	defer resp.Body.Close()
	doc, err := goquery.NewDocumentFromReader(resp.Body)
	if err != nil {
		util.FatalLogf("failed to parse %s: %v", url, err)
	}
	return doc
}

func GetJson(url string, v any) {
	resp := getResp(url)
	bytes, err := io.ReadAll(resp.Body)
	_ = resp.Body.Close()
	if err != nil {
		util.FatalLogf("failed to read %s: %v", url, err)
	}
	err = json.Unmarshal(bytes, v)
	if err != nil {
		util.FatalLogf("failed to parse %s: %v", url, err)
	}
}

func getResp(url string) *http.Response {
	resp, err := http.Get(url)
	if err != nil || resp.StatusCode != 200 {
		util.FatalLogf("failed to get %s: %v", url, err)
	}
	return resp
}
