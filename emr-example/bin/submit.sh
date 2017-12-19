/usr/local/service/spark/bin/spark-submit \
--master yarn \
--deploy-mode cluster \
--class  com.tencent.cloud.emr.CosLogParser \
--conf spark.locality.wait=10 \
--executor-memory 16g \
--num-executors  6  \
--executor-cores 4 \
--conf spark.shuffle.memoryFraction=0.9 \
--conf spark.storage.memoryFraction=0.1 \
--driver-memory 10g  \
--conf spark.kryoserializer.buffer.max=512m \
--conf spark.sql.crossJoin.enabled=true \
 --conf spark.default.parallelism=60 \
--conf spark.sql.shuffle.partitions=8 \
--conf spark.io.compression.codec=snappy \
--conf spark.serializer=org.apache.spark.serializer.KryoSerializer \
--conf spark.sql.inMemoryColumnarStorage.compressed=true \
--conf spark.sql.inMemoryColumnarStorage.batchSize=10000 \
--conf "spark.executor.extraJavaOptions=-Xmn6g -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:GCPauseIntervalMillis=100" \
--conf "spark.driver.extraJavaOptions=-XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:GCPauseIntervalMillis=100"  \
/usr/local/service/hadoop/emr-example-1.0-SNAPSHOT.jar /user/root/*/*.log /cos/output