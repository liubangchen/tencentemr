package configure

/**
@auth liubangche
配置文件模块
*/
import (
	logger "github.com/shengkehua/xlog4go"
	"io/ioutil"
	"os"
	"path"
	"path/filepath"
	"strconv"
	"strings"
	"errors"
	"panda/util"
	"time"
)

const conflogger = "conf"

type Configure struct {
	confmap map[string]map[string]string //配置文件map，key为配置文件名，value为key和value的map
	timemap map[string]int64             //配置文件最后修改时间
}

var conf *Configure

func reloadConf() {
	go func() {
		for {
			filepath.Walk(conf.getRoot(), func(path string, file os.FileInfo, err error) error {
				if file == nil {
					return err
				}
				if file.IsDir() {
					return nil
				}
				filename := file.Name()
				extname := filepath.Ext(filename)
				if extname != ".properties" {
					return nil
				}
				modtime, ok := conf.timemap[filename]
				curmodtime := file.ModTime().Unix()
				if curmodtime > modtime || !ok {
					conf.timemap[filename] = curmodtime
					conf.readFile(filename)
				}
				return nil
			})
			time.Sleep(3 * time.Second)
		}
	}()
}

/**
* 模块初始化，由系统调用
 */
func init() {
	conf = &Configure{confmap: map[string]map[string]string{}, timemap: map[string]int64{}}
	err := conf.loadConf()
	if err != nil {
		logger.Error("init conf err:", err)
		os.Exit(-1)
	}
}

func (this *Configure) loadConf() (error) {
	filepath.Walk(this.getRoot(), func(path string, file os.FileInfo, err error) error {
		if file == nil {
			return err
		}
		if file.IsDir() {
			logger.Info("0000")
			return nil
		}
		filename := file.Name()
		extname := filepath.Ext(filename)
		if extname != ".properties" {
			return nil
		}
		conf.timemap[filename] = file.ModTime().Unix()
		this.readFile(filename)
		return nil
	})
	reloadConf()
	return nil
}

func (this *Configure) getRoot() (string) {
	return path.Join(util.GetRootDir(), "etc")
}

func (this *Configure) readFile(filename string) {

	fullname := path.Join(this.getRoot(), filename)
	buf, err := ioutil.ReadFile(fullname)
	if err != nil {
		logger.Error("configure readFile error conffile:", fullname, ",err:", err)
		return
	}
	settings := strings.Split(string(buf), "\n")
	if len(settings) == 0 {
		logger.Error("configure readFile error,file is empty conffile:", filename)
		return
	}
	for i := 0; i < len(settings); i++ {
		this.parseConf(filename, settings[i])
	}
}

func (this *Configure) GetValue(filename, key string) string {
	cmap, ok := this.confmap[filename]
	if !ok {
		logger.Error("configure GetConfMap error,can't find conf:" + filename)
		return ""
	}
	v, ok := cmap[key]
	if !ok {
		return ""
	}
	return v
}

func CreateConfigure(filename string) (c *Configure, err error) {

	buf, err := ioutil.ReadFile(filename)
	if err != nil {
		logger.Error("configure readFile error conffile:", filename, ",err:", err)
		return nil, err
	}
	settings := strings.Split(string(buf), "\n")
	if len(settings) == 0 {
		logger.Error("configure readFile error,file is empty conffile:", filename)
		return nil, errors.New("setting is empty")
	}
	c = &Configure{confmap: map[string]map[string]string{}, timemap: map[string]int64{}}
	for i := 0; i < len(settings); i++ {
		c.parseConf(filename, settings[i])
	}
	return c, nil
}

func GetConfMap(filename string) map[string]string {
	cmap, ok := conf.confmap[filename]
	if !ok {
		logger.Error("configure GetConfMap error,can't find conf:" + filename)
		return map[string]string{}
	}
	return cmap
}

func GetConfStringValue(filename, key string) string {
	cmap, ok := conf.confmap[filename]
	if !ok {
		logger.Error("configure GetConfStringValue error,can't find conffile:" + filename)
		return ""
	}
	value, ok := cmap[key]
	if !ok {
		logger.Error("configure GetConfStringValue error,can't find key:" + key + ",file:" + filename)
		return ""
	}
	return value
}

func GetConfStringValueWithDefault(filename, key string, value string) string {
	v := GetConfStringValue(filename, key)
	if len(v) == 0 {
		return value
	}
	return v
}

func GetConfIntValue(filename, key string, dv int) int {
	value := GetConfStringValue(filename, key)
	if len(value) == 0 {
		return dv
	}
	i, err := strconv.Atoi(value)
	if err != nil {
		logger.Error("configure GetConfIntValue error, key:" + key + ",file:" + filename)
		return 0
	}
	return i
}

func GetConfFloatValue(filename, key string, dv float64) float64 {
	value := GetConfStringValue(filename, key)
	if len(value) == 0 {
		return dv
	}
	i, err := strconv.ParseFloat(value, 64)
	if err != nil {
		logger.Error("configure GetConfIntValue error, key:" + key + ",file:" + filename)
		return 0
	}
	return i
}

func GetConfInt64Value(filename, key string, dv int64) int64 {
	value := GetConfStringValue(filename, key)
	if len(value) == 0 {
		return dv
	}
	i, err := strconv.ParseInt(value, 0, 64)
	if err != nil {
		logger.Error("configure GetConfInt64Value error, key:" + key + ",file:" + filename)
		return 0
	}
	return i
}

func GetConfBoolValue(filename, key string, b bool) bool {
	value := GetConfStringValue(filename, key)
	if len(value) == 0 {
		return b
	}
	if value == "true" {
		return true
	}
	return false
}

func (this *Configure) parseConf(filename, conf string) {
	conf = strings.TrimSpace(conf)
	if len(conf) == 0 {
		return
	}
	index := strings.Index(conf, "#")
	if index == 0 {
		return
	}
	index = strings.Index(conf, "=")
	if index < 0 {
		logger.Error("conf setting  is error ,file:" + filename + " conf setting:" + conf)
		return
	}
	key := strings.TrimSpace(string(conf[0:index]))
	value := strings.TrimSpace(string(conf[index + 1 : len(conf)]))
	if len(key) <= 0 {
		logger.Error("conf setting  is error ,file:" + filename + " conf setting:" + conf)
		return
	}

	if confmap, ok := this.confmap[filename]; ok {
		confmap[key] = value
	} else {
		confmap = map[string]string{}
		confmap[key] = value
		this.confmap[filename] = confmap
	}
}
