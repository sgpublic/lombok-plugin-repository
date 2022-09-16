package git

import (
	"context"
	"github.com/google/go-github/v47/github"
	log "github.com/sirupsen/logrus"
	"golang.org/x/oauth2"
	"os"
	"strings"
)

var (
	TOKEN      string
	REPO       string
	REPO_OWNER string
	REPO_NAME  string

	ctx     context.Context
	service *github.RepositoriesService
)

func Init() {
	if TOKEN == "" || REPO == "" {
		log.Fatalln("Arg required!")
	}
	split := strings.Split(REPO, "/")
	if len(split) != 2 {
		log.Fatalln("Unknown REPO!")
	}
	REPO_OWNER = split[0]
	REPO_NAME = split[1]

	ctx = context.Background()
	ts := oauth2.StaticTokenSource(&oauth2.Token{
		AccessToken: TOKEN,
	})
	tc := oauth2.NewClient(ctx, ts)
	service = github.NewClient(tc).Repositories
	_, _, err := service.Get(ctx, REPO_OWNER, REPO_NAME)
	if err != nil {
		log.Fatalf("unable to touch repo of %s/%s", REPO_OWNER, REPO_NAME)
	}
}

func HasTag(tag string) bool {
	_, _, err := service.GetReleaseByTag(ctx, REPO_OWNER, REPO_NAME, tag)
	return err == nil
}

var (
	TargetCommitish      = "master"
	Draft                = false
	Prerelease           = false
	GenerateReleaseNotes = false
)

func CreateTag(tag string, verNames []string, filePath string) error {
	file, err := os.Open(filePath)
	if err != nil {
		return err
	}
	body := "+ " + strings.Join(verNames, "\n+ ")
	release, _, err := service.CreateRelease(ctx, REPO_OWNER, REPO_NAME, &github.RepositoryRelease{
		TagName:              &tag,
		TargetCommitish:      &TargetCommitish,
		Name:                 &tag,
		Body:                 &body,
		Draft:                &Draft,
		Prerelease:           &Prerelease,
		GenerateReleaseNotes: &GenerateReleaseNotes,
	})
	if err != nil {
		return err
	}
	_, _, err = service.UploadReleaseAsset(
		ctx, REPO_OWNER, REPO_NAME, *release.ID,
		&github.UploadOptions{
			Name: "lombok-" + tag + ".tar.gz",
		}, file)
	return err
}
