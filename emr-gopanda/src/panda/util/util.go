package util

import (
	"path/filepath"
	"os"
)

func GetRootDir() string {
	dir, err := filepath.Abs(filepath.Dir(os.Args[0]))
	if err != nil {
		panic(err)
	}
	dir = filepath.Join(dir, "../") + "/"
	return dir
}
