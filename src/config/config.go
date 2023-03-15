package config

import (
	"flag"
	rotatelogs "github.com/lestrrat-go/file-rotatelogs"
	"github.com/mattn/go-colorable"
	log "github.com/sirupsen/logrus"
	"gopkg.in/yaml.v3"
	"lombok-plugin-action/src/util"
	"os"
	"os/exec"
	"path"
	"path/filepath"
	"strings"
	"time"
)

var (
	_ConfigPath string
	_Debug      bool
	_Service    bool
)

func Setup() {
	_SetupFlag()
	_SetupConfig()
	if _Service {
		_Daemon()
	}
}

func _SetupFlag() {
	flag.StringVar(&_ConfigPath, "config", "./config.yaml", "Set the config file (*.yaml) to use.")
	flag.BoolVar(&_Debug, "debug", false, "Debug mod")
	flag.BoolVar(&_Service, "service", false, "Service mod")
	flag.Parse()
}

func IsDebug() bool {
	return _Debug
}

func IsService() bool {
	return _Service
}

func Cron() string {
	return _ReadConfig().Cron
}

func GithubRepo() string {
	return _ReadConfig().Github.Repository
}

func GithubToken() string {
	return _ReadConfig().Github.Token
}

func TempDir() string {
	dir := _ReadConfig().TmpDir
	if !strings.HasSuffix(dir, "/") {
		dir = dir + "/"
	}
	return dir
}

func _SetupConfig() {
	conf := _ReadConfig()

	log.SetOutput(colorable.NewColorableStdout())
	log.SetFormatter(util.LogFormat{EnableColor: true})

	log.RegisterExitHandler(func() {
		_ = os.RemoveAll(conf.TmpDir)
	})

	err := os.MkdirAll(conf.TmpDir, 0744)
	if err != nil && !os.IsExist(err) {
		log.Fatalf("Tmp dir create faild: %s\n%v", conf.TmpDir, err)
	}

	rotateOptions := []rotatelogs.Option{
		rotatelogs.WithRotationTime(time.Hour * 24),
	}
	rotateOptions = append(rotateOptions, rotatelogs.WithMaxAge(time.Duration(conf.Logging.Aging)*time.Second))
	err = os.MkdirAll(conf.Logging.Path, 0744)
	if err != nil && !os.IsExist(err) {
		log.Errorf("log dir init err: %v", err)
		return
	}
	w, err := rotatelogs.New(path.Join(conf.Logging.Path, "%Y-%m-%d.log"), rotateOptions...)
	if err != nil {
		log.Errorf("rotatelogs init err: %v", err)
	} else {
		log.AddHook(util.NewLocalHook(w, IsDebug()))
	}

	// debug mode
	if IsDebug() {
		log.SetLevel(log.DebugLevel)
		log.Debug("Enable debug mode!")
	} else {
		log.SetLevel(log.InfoLevel)
	}
}

func _Daemon() {
	args := os.Args[1:]
	execArgs := make([]string, 0)
	l := len(args)
	for i := 0; i < l; i++ {
		if strings.Compare(args[i], "-service") == 0 {
			continue
		}
		execArgs = append(execArgs, args[i])
	}

	ex, _ := os.Executable()
	p, _ := filepath.Abs(ex)
	proc := exec.Command(p, execArgs...)
	err := proc.Start()
	if err != nil {
		panic(err)
	}
	log.Infof("[PID] %d", proc.Process.Pid)
	os.Exit(0)
}

func _ReadConfig() _ConfigRoot {
	conf := new(_ConfigRoot)
	yamlFile, err := os.ReadFile(_ConfigPath)
	if err != nil {
		log.Fatalf("Config file '%s' not found, please create one first!", _ConfigPath)
	}
	err = yaml.Unmarshal(yamlFile, conf)
	if err != nil {
		log.Fatalf("Config file read failed: %v", err)
	}
	return *conf
}

type _ConfigRoot struct {
	Logging struct {
		Path  string `yaml:"path" default:"/var/log/lombok"`
		Aging int    `yaml:"aging" default:"604800"`
	} `yaml:"logging"`

	Github struct {
		Token      string `yaml:"token"`
		Repository string `yaml:"repository"`
	} `yaml:"github"`

	Cron string `yaml:"cron"`

	TmpDir string `yaml:"tmp-dir" default:"/tmp/lombok-plugin"`
}
