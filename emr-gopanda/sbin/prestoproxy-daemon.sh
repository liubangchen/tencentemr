#!/usr/bin/env bash

export PRESTOPROXY_HOME=/usr/local/service/prestoproxy
execp=$PRESTOPROXY_HOME/bin/prestoproxy
echo "$excp"
chmod u+x $execp


start_proxy()
{
    $execp  > /dev/null 2>&1 &
    sleep 5
    alive=`ps aux  | grep prestoproxy | grep -v prestoproxy-daemon.sh | grep -v grep | wc -l`
    echo $alive
    if [ $alive -eq 1 ]
    then
            echo "start success!"
    else
            echo "start falied"
            exit 1
    fi
}

stop_proxy()
{
    killall -TERM prestoproxy
    sleep 3
    alive=`ps aux  | grep prestoproxy | grep -v prestoproxy-daemon.sh | grep -v grep | wc -l`
    if [ $alive -eq 0 ]
    then
            echo "stop success!"
    else
            echo "stop falied"
            exit 1
    fi
}

if [ $# -ne 1 ]
then
    echo "usage: prestoproxy-daemon.sh start/stop/restart"
    exit 1
fi

case $1 in
    start)
        start_proxy
        ;;
    stop)
        stop_proxy
        ;;
    restart)
    echo "restart"
        stop_proxy
        start_proxy
        ;;
esac
