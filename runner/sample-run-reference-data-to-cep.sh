#/bin/bash
JAR=tsel-kafka-0.1.0.jar
LOCAL_EXECUTOR_PATH_TO_JAR="/home/cloudera/$JAR"
KF_BROKER_LIST="quickstart:9092"

spark-submit --class com.cloudera.sa.tsel.RunReferenceDataToCEP \
    --master yarn-cluster \
    --conf "spark.executor.extraClassPath=$LOCAL_EXECUTOR_PATH_TO_JAR" \
    --conf spark.yarn.executor.memoryOverhead=2048 --executor-memory 2G --executor-cores 2 \
    --conf spark.akka.frameSize=350 \
    $JAR \
    --topic "fms-test-spark-hive-kafka-channel" \
    --delimiter "|" \
    --datafeed "datafeed-customerdata" \
    --sql "SELECT * FROM smy.ocs_special_no_list_snap LIMIT 5" \
    --kafka-broker-list $KF_BROKER_LIST \
    --debug-config
