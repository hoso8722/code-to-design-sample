#!/bin/bash
# 第8回の出力を集め直す。JAVA_HOME か PATH に JDK 21 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" exam/*.java
java -cp "$WORK" exam.LazyDemo > output-lazy.txt
java -cp "$WORK" exam.SecureDemo > output-secure.txt
java -cp "$WORK" exam.DynamicDemo > output-dynamic.txt
java -cp "$WORK" exam.Bench > output-bench.txt
rm -rf "$WORK"
