#!/bin/bash

libs=$(ls -al /usr/local/service/hbase/lib/*.jar |awk '{print $9}')

classpath="./emr-example-1.0-SNAPSHOT.jar"

for lib in ${libs[@]};do
    classpath="${classpath}:${lib}"	
done 

java -Xmn1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:GCPauseIntervalMillis=100 -classpath $classpath com.tencent.cloud.emr.Main "$@"

