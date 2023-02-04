package plugin

import (
	"encoding/xml"
	"github.com/emirpasic/gods/maps/hashmap"
	"github.com/emirpasic/gods/queues/priorityqueue"
	"lombok-plugin-action/src/git/github"
	"lombok-plugin-action/src/versions/iu"
	"os"
	"strings"
	"time"
)

type _IdeaPluginName struct {
	Text string `xml:",chardata"`
}
type _IdeaPluginId struct {
	Text string `xml:",chardata"`
}
type _IdeaPluginDescription struct {
	Text string `xml:",chardata"`
}
type _IdeaPluginVersion struct {
	Text string `xml:",chardata"`
}
type _IdeaPluginVendor struct {
	Text  string `xml:",chardata"`
	Email string `xml:"email,attr"`
	URL   string `xml:"url,attr"`
}
type _IdeaPluginRating struct {
	Text string `xml:",chardata"`
}
type _IdeaPluginChangeNotes struct {
	Text string `xml:",chardata"`
}
type _IdeaPluginDownloadURL struct {
	Text string `xml:",chardata"`
}
type _IdeaPluginIdeaVersion struct {
	Text       string `xml:",chardata"`
	Min        string `xml:"min,attr"`
	Max        string `xml:"max,attr"`
	SinceBuild string `xml:"since-build,attr"`
	UntilBuild string `xml:"until-build,attr"`
}

type _IdeaPlugin struct {
	Downloads   int                    `xml:"downloads,attr"`
	Size        int                    `xml:"size,attr"`
	Date        int64                  `xml:"date,attr"`
	UpdatedDate int64                  `xml:"updatedDate,attr"`
	URL         string                 `xml:"url,attr"`
	Name        _IdeaPluginName        `xml:"name"`
	ID          _IdeaPluginId          `xml:"id"`
	Description _IdeaPluginDescription `xml:"description"`
	Version     _IdeaPluginVersion     `xml:"version"`
	Vendor      _IdeaPluginVendor      `xml:"vendor"`
	Rating      _IdeaPluginRating      `xml:"rating"`
	ChangeNotes _IdeaPluginChangeNotes `xml:"change-notes"`
	DownloadURL _IdeaPluginDownloadURL `xml:"download-url"`
	IdeaVersion _IdeaPluginIdeaVersion `xml:"idea-version"`
}

type _PluginRepositoryFf struct {
	Text string `xml:",chardata"`
}
type _PluginRepositoryCategory struct {
	Text       string        `xml:",chardata"`
	Name       string        `xml:"name,attr"`
	IdeaPlugin []_IdeaPlugin `xml:"idea-plugin"`
}

type _PluginRepository struct {
	XMLName  xml.Name                  `xml:"plugin-repository"`
	Ff       _PluginRepositoryFf       `xml:"ff"`
	Category _PluginRepositoryCategory `xml:"category"`
	Text     string                    `xml:",chardata"`
}

func CreateRepositoryXml(verTags *priorityqueue.Queue, verInfos *hashmap.Map, sizes *hashmap.Map) (string, error) {
	content := _PluginRepository{
		Ff: _PluginRepositoryFf{Text: "\"Tools Integration\""},
	}
	var categories []_IdeaPlugin

	var item interface{}
	var hasNext bool
	for {
		item, hasNext = verTags.Dequeue()
		if !hasNext {
			break
		}
		verTag := item.(string)
		size, found := sizes.Get(verTag)
		if !found {
			continue
		}
		tmp, _ := verInfos.Get(verTag)
		release := tmp.(iu.IdeaRelease)
		date, _ := time.Parse("2006-01-02", release.Date)
		unix := date.Unix() * 1000
		untilBuild := strings.Split(verTag, ".")[0] + ".*"
		categories = append(categories, _IdeaPlugin{
			Downloads:   0,
			Size:        size.(int),
			Date:        unix,
			UpdatedDate: unix,
			URL:         "https://github.com/" + github.REPO,
			Name:        _IdeaPluginName{Text: "Lombok"},
			ID:          _IdeaPluginId{Text: "Lombook Plugin"},
			Description: _IdeaPluginDescription{
				Text: "<![CDATA[<h1>IntelliJ Lombok plugin</h1>" +
					"<br/>" +
					"<a href=\"https://github.com/mplushnikov/lombok-intellij-plugin\">GitHub</a> |" +
					"<a href=\"https://github.com/mplushnikov/lombok-intellij-plugin/issues\">Issues</a> | Donate (" +
					"<a href=\"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3F9HXD7A2SMCN\">PayPal</a> )" +
					"<br/>" +
					"<br/>" +
					"" +
					"<b>A plugin that adds first-class support for Project Lombok</b>" +
					"<br/>" +
					"<br/>" +
					"" +
					"<b>Features</b>" +
					"<ul>" +
					"  <li><a href=\"https://projectlombok.org/features/GetterSetter.html\">@Getter and @Setter</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/experimental/FieldNameConstants\">@FieldNameConstants</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/ToString.html\">@ToString</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/EqualsAndHashCode.html\">@EqualsAndHashCode</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/Constructor.html\">@AllArgsConstructor, @RequiredArgsConstructor and" +
					"    @NoArgsConstructor</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/Log.html\">@Log, @Log4j, @Log4j2, @Slf4j, @XSlf4j, @CommonsLog," +
					"    @JBossLog, @Flogger, @CustomLog</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/Data.html\">@Data</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/Builder.html\">@Builder</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/experimental/SuperBuilder\">@SuperBuilder</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/Builder.html#singular\">@Singular</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/Delegate.html\">@Delegate</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/Value.html\">@Value</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/experimental/Accessors.html\">@Accessors</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/experimental/Wither.html\">@Wither</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/With.html\">@With</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/SneakyThrows.html\">@SneakyThrows</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/val.html\">@val</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/var.html\">@var</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/experimental/var.html\">experimental @var</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/experimental/UtilityClass.html\">@UtilityClass</a></li>" +
					"  <li><a href=\"https://projectlombok.org/features/configuration.html\">Lombok config system</a></li>" +
					"  <li>Code inspections</li>" +
					"  <li>Refactoring actions (lombok and delombok)</li>" +
					"</ul>" +
					"<br/>]]>",
			},
			Version: _IdeaPluginVersion{Text: verTag},
			Vendor: _IdeaPluginVendor{
				URL:   "https://github.com/" + github.REPO + "/release/tag/" + verTag,
				Email: "",
			},
			Rating:      _IdeaPluginRating{Text: "5.0"},
			ChangeNotes: _IdeaPluginChangeNotes{Text: "<![CDATA[]]>"},
			DownloadURL: _IdeaPluginDownloadURL{Text: "https://github.com/" + github.REPO + "/release/download/" + verTag + "/lombok-" + verTag + ".zip"},
			IdeaVersion: _IdeaPluginIdeaVersion{
				Max:        "n/a",
				Min:        "n/a",
				SinceBuild: verTag,
				UntilBuild: untilBuild,
			},
		})
	}

	content.Category = _PluginRepositoryCategory{
		Name:       "Tools Integration",
		IdeaPlugin: categories,
	}

	name := "/tmp/lombok-plugin/plugin-repository"
	_, err := os.Stat(name)
	if err == nil {
		err = os.Remove(name)
	}
	if err != nil && !os.IsNotExist(err) {
		return "", err
	}

	file, err := os.OpenFile(name, os.O_CREATE|os.O_WRONLY, 0744)
	defer file.Close()
	if err != nil {
		return "", err
	}
	err = xml.NewEncoder(file).Encode(content)
	if err != nil {
		return "", err
	}
	return name, nil
}
