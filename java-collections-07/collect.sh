#!/bin/bash
# 第7回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
# JMH のJARファイル4つを置いたフォルダを、環境変数 JMH_LIB に指定する
set -e
cd "$(dirname "$0")"
echo "JDK: $(java -version 2>&1 | sed -n 2p)"
CP="$JMH_LIB/jmh-core-1.37.jar:$JMH_LIB/jmh-generator-annprocess-1.37.jar:$JMH_LIB/jopt-simple-5.0.4.jar:$JMH_LIB/commons-math3-3.6.1.jar"
WORK=$(mktemp -d)
javac -cp "$CP" -processorpath "$CP" -d "$WORK/classes" src/bench/StreamVsLoop.java
java -cp "$WORK/classes:$CP" org.openjdk.jmh.Main bench.StreamVsLoop -prof gc -rf csv -rff jmh-StreamVsLoop.csv > jmh-StreamVsLoop.txt 2>&1
java src/Lazy.java > lazy.txt
rm -rf "$WORK"
