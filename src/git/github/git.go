package github

import (
	"context"
	"github.com/google/go-github/v50/github"
	log "github.com/sirupsen/logrus"
	"golang.org/x/oauth2"
	"lombok-plugin-action/src/config"
	"lombok-plugin-action/src/lombok"
	"lombok-plugin-action/src/util"
	"lombok-plugin-action/src/versions/as"
	"os"
	"strings"
)

var (
	REPO_OWNER string
	REPO_NAME  string

	ctx     context.Context
	service *github.RepositoriesService

	TargetCommitish      = "master"
	GenerateReleaseNotes = false
)

func Init() {
	if config.GithubToken() == "" || config.GithubRepo() == "" {
		util.FatalLogln("Arg required!")
	}
	split := strings.Split(config.GithubRepo(), "/")
	if len(split) != 2 {
		util.FatalLogln("Unknown REPO!")
	}
	REPO_OWNER = split[0]
	REPO_NAME = split[1]

	ctx = context.Background()
	ts := oauth2.StaticTokenSource(&oauth2.Token{
		AccessToken: config.GithubToken(),
	})
	tc := oauth2.NewClient(ctx, ts)
	service = github.NewClient(tc).Repositories
	_, _, err := service.Get(ctx, REPO_OWNER, REPO_NAME)
	if err != nil {
		util.FatalLogf("unable to touch repo of %s/%s", REPO_OWNER, REPO_NAME)
	}
}

func GetReleaseByTag(tag string) (*github.RepositoryRelease, error) {
	release, _, err := service.GetReleaseByTag(ctx, REPO_OWNER, REPO_NAME, tag)
	return release, err
}

func UpdateReleaseBody(release *github.RepositoryRelease) error {
	_, _, err := service.EditRelease(ctx, REPO_OWNER, REPO_NAME, *release.ID, release)
	return err
}

var (
	PluginRepositoryName = "plugin-repository"
)

func CreatePluginRepository(path string, forceUpdate bool) error {
	file, err := os.Open(path)
	if err != nil {
		return err
	}
	release, _, _ := service.GetReleaseByTag(ctx, REPO_OWNER, REPO_NAME, PluginRepositoryName)
	if release == nil {
		if err != nil {
			return err
		}
		body := "Lombok plugin repository, see README.md."
		release, _, err = service.CreateRelease(ctx, REPO_OWNER, REPO_NAME, &github.RepositoryRelease{
			TagName:              &PluginRepositoryName,
			TargetCommitish:      &TargetCommitish,
			Name:                 &PluginRepositoryName,
			Body:                 &body,
			GenerateReleaseNotes: &GenerateReleaseNotes,
		})
		if err != nil {
			return err
		}
	} else {
		if !forceUpdate {
			return nil
		}
		for _, asset := range release.Assets {
			_, err := service.DeleteReleaseAsset(ctx, REPO_OWNER, REPO_NAME, *asset.ID)
			if err != nil {
				return err
			}
		}
	}
	_, _, err = service.UploadReleaseAsset(
		ctx, REPO_OWNER, REPO_NAME, *release.ID,
		&github.UploadOptions{
			Name: PluginRepositoryName,
		}, file)
	file.Close()
	return err
}

func CreateTag(tag string, versions []as.AndroidStudioRelease, zipFile string) error {
	log.Infof("Start uploading version %s...", tag)
	file, err := os.Open(zipFile)
	if err != nil {
		return err
	}
	body, prerelease := lombok.CreateReleaseNote(versions)
	release, _, err := service.CreateRelease(ctx, REPO_OWNER, REPO_NAME, &github.RepositoryRelease{
		TagName:              &tag,
		TargetCommitish:      &TargetCommitish,
		Name:                 &tag,
		Body:                 &body,
		Prerelease:           &prerelease,
		GenerateReleaseNotes: &GenerateReleaseNotes,
	})
	if err != nil {
		return err
	}
	_, _, err = service.UploadReleaseAsset(
		ctx, REPO_OWNER, REPO_NAME, *release.ID,
		&github.UploadOptions{
			Name: "lombok-" + tag + ".zip",
		}, file)
	return err
}
