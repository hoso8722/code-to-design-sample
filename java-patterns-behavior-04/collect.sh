#!/bin/bash
# 第4回の出力を集め直す。JAVA_HOME か PATH に JDK 25 を置いて、このフォルダの中で実行する
set -e
cd "$(dirname "$0")"
WORK=$(mktemp -d)
javac -d "$WORK" exam/*.java
java -cp "$WORK" exam.Demo > output-demo.txt
java -cp "$WORK" exam.Lambdas > output-lambdas.txt
java -cp "$WORK" exam.MissingTarget > output-missing-target.txt
: > output-memory.txt
for mode in snapshot command; do java -Xmx1g -cp "$WORK" exam.Memory $mode >> output-memory.txt; done
rm -rf "$WORK"
