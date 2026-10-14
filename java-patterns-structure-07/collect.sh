#!/bin/bash
# 第7回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK/inherit" inherit/exam/*.java
mkdir -p "$WORK/d"
javac -d "$WORK/d" decorator/exam/*.java
python3 count.py > output-count.txt
java -cp "$WORK/d" exam.Order > output-order.txt
java -cp "$WORK/d" exam.Speed > output-speed.txt
java jdk/exam/JdkDecorators.java > output-jdk.txt
rm -rf "$WORK"
