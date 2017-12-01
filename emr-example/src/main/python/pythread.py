#!/usr/bin/python
# -*- coding: utf-8 -*-

from threading import Thread
import subprocess


def doCommand(cmd):
    if cmd == "":
        return 0
    cmdp = subprocess.Popen(
        cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    for line in cmdp.stdout.readlines():
        print line

    return cmdp.wait()


def execquery(name, *args):
    cmd = "/usr/local/service/spark/bin/beeline -u jdbc:hive2://10.59.168.84:7101 -f ./query.sql"
    doCommand(cmd)


if __name__ == '__main__':

    try:
        ts = []

        for i in range(0, 4):
            t = Thread(None, execquery, None, ('query', i))
            t.start()
            ts.append(t)

        for th in ts:
            th.join()

    except Exception as errtxt:
        print errtxt
