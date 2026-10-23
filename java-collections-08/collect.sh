#!/bin/bash
# 第8回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
# JMH のJARファイル4つを置いたフォルダを、環境変数 JMH_LIB に指定する
set -e
cd "$(dirname "$0")"
echo "JDK: $(java -version 2>&1 | sed -n 2p)"
CP="$JMH_LIB/jmh-core-1.37.jar:$JMH_LIB/jmh-generator-annprocess-1.37.jar:$JMH_LIB/jopt-simple-5.0.4.jar:$JMH_LIB/commons-math3-3.6.1.jar"
WORK=$(mktemp -d)
javac -cp "$CP" -processorpath "$CP" -d "$WORK/classes" src/bench/ParallelSource.java src/bench/ParallelSum.java
java -cp "$WORK/classes:$CP" org.openjdk.jmh.Main bench.ParallelSource -rf csv -rff jmh-ParallelSource.csv > jmh-ParallelSource.txt 2>&1
java -cp "$WORK/classes:$CP" org.openjdk.jmh.Main bench.ParallelSum -rf csv -rff jmh-ParallelSum.csv > jmh-ParallelSum.txt 2>&1
java src/Pitfalls.java > pitfalls.txt
rm -rf "$WORK"
