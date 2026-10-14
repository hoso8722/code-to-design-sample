#!/bin/bash
# 第4回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -g -d "$WORK" src/GcDemo.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"
for mode in short long huge; do
  rm -f gc-$mode.log
  java -Xmx512m -Xlog:gc*:file=gc-$mode.log:uptime,level,tags:filecount=0 -cp "$WORK" GcDemo $mode
  python3 parse-gc-log.py gc-$mode.log > pauses-$mode.csv
done
rm -rf "$WORK"
