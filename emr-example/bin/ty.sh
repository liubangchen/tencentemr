#!/bin/bash
# set environment variables (if not already done)
export PYTHON_ROOT=./Python
export LD_LIBRARY_PATH=${PATH}
export PYSPARK_PYTHON=${PYTHON_ROOT}/bin/python
export SPARK_YARN_USER_ENV="PYSPARK_PYTHON=Python/bin/python"
export PATH=${PYTHON_ROOT}/bin/:$PATH
export QUEUE=gpu

# set paths to libjvm.so, libhdfs.so, and libcuda*.so
#export LIB_HDFS=/opt/cloudera/parcels/CDH/lib64                      # for CDH (per @wangyum)
export LIB_HDFS=$HADOOP_PREFIX/lib/native/Linux-amd64-64
export LIB_JVM=$JAVA_HOME/jre/lib/amd64/server
export LIB_CUDA=/usr/local/cuda-7.5/lib64

# for CPU mode:
# export QUEUE=default
# remove references to $LIB_CUDA

# save images and labels as CSV files
${SPARK_HOME}/bin/spark-submit \
 --master yarn \
 --deploy-mode cluster \
 --queue ${QUEUE} \
 --num-executors 4 \
 --executor-memory 4G \
 --archives hdfs:///user/${USER}/Python.zip#Python,mnist/mnist.zip#mnist \
 --conf spark.executorEnv.LD_LIBRARY_PATH=$LIB_CUDA \
 --driver-library-path=$LIB_CUDA \
 TensorFlowOnSpark/examples/mnist/mnist_data_setup.py \
 --output mnist/csv \
 --format csv

# save images and labels as TFRecords (OPTIONAL)
${SPARK_HOME}/bin/spark-submit \
 --master yarn \
 --deploy-mode cluster \
 --queue ${QUEUE} \
 --num-executors 4 \
 --executor-memory 4G \
 --archives hdfs:///user/${USER}/Python.zip#Python,mnist/mnist.zip#mnist \
 --jars hdfs:///user/${USER}/tensorflow-hadoop-1.0-SNAPSHOT.jar \
 --conf spark.executorEnv.LD_LIBRARY_PATH=$LIB_CUDA \
 --driver-library-path=$LIB_CUDA \
 TensorFlowOnSpark/examples/mnist/mnist_data_setup.py \
 --output mnist/tfr \
 --format tfr