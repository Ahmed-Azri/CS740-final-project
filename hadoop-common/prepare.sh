#!/bin/bash

export HADOOP_COMMON_HOME=$(pwd)/$(ls -d hadoop-common-project/hadoop-common/target/hadoop-common-*-SNAPSHOT)
export HADOOP_HDFS_HOME=$(pwd)/$(ls -d hadoop-hdfs-project/hadoop-hdfs/target/hadoop-hdfs-*-SNAPSHOT)
export PATH=$HADOOP_COMMON_HOME/bin:$HADOOP_HDFS_HOME/bin:$PATH
