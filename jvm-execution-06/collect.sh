#!/bin/bash
# 第6回の計測データを集め直す。JAVA_HOME か PATH に、計測したいJDKを置いて、このフォルダの中で実行する
# JMH のJARファイル4つを置いたフォルダを、環境変数 JMH_LIB に指定する(約7分半かかる)
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" src/NaiveBench.java
echo "JDK: $(java -version 2>&1 | sed -n 2p)"

# 1. 単純なベンチマーク(2つの順番を、各3回)
: > naive.txt
for order in plus-first plus-first plus-first builder-first builder-first builder-first; do
  java -cp "$WORK" NaiveBench $order >> naive.txt
done

# 2. JMH
CP="$JMH_LIB/jmh-core-1.37.jar:$JMH_LIB/jmh-generator-annprocess-1.37.jar:$JMH_LIB/jopt-simple-5.0.4.jar:$JMH_LIB/commons-math3-3.6.1.jar"
javac -cp "$CP" -processorpath "$CP" -d "$WORK/classes" src/bench/StringConcatBench.java
java -cp "$WORK/classes:$CP" org.openjdk.jmh.Main bench.StringConcatBench -rf csv -rff jmh-result.csv > jmh-output.txt 2>&1
rm -rf "$WORK"
